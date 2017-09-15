/**
 * Copyright 2015, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.workflowexecutor.executor

import java.io._
import java.net.InetAddress

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}
import scala.io.Source
import scala.util.{Failure, Success, Try}

import akka.actor.ActorSystem
import spray.json._

import io.deepsense.commons.models.Entity
import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang._
import io.deepsense.graph.CyclicGraphException
import io.deepsense.models.json.workflow.exceptions._
import io.deepsense.models.workflows.{ExecutionReport, WorkflowWithResults, WorkflowWithVariables}
import io.deepsense.workflowexecutor.WorkflowExecutorActor.Messages.Launch
import io.deepsense.workflowexecutor.WorkflowExecutorApp._
import io.deepsense.workflowexecutor._
import io.deepsense.workflowexecutor.exception.{UnexpectedHttpResponseException, WorkflowExecutionException}
import io.deepsense.workflowexecutor.pyspark.PythonPathGenerator
import io.deepsense.workflowexecutor.session.storage.DataFrameStorageImpl

/**
 * WorkflowExecutor creates an execution context and then executes a workflow on Spark.
 */
case class WorkflowExecutor(
    workflow: WorkflowWithVariables,
    pythonExecutorPath: String,
    pythonPathGenerator: PythonPathGenerator)
  extends Executor {

  val dOperableCache = mutable.Map[Entity.Id, DOperable]()
  private val actorSystemName = "WorkflowExecutor"

  def execute(): Try[ExecutionReport] = Try {

    if (workflow.graph.containsCycle) {
      val cyclicGraphException = new CyclicGraphException
      logger.error("WorkflowExecutorActor failed due to incorrect workflow: ", cyclicGraphException)
      throw cyclicGraphException
    }

    val dataFrameStorage = new DataFrameStorageImpl

    val sparkContext = createSparkContext()
    val sqlContext = createSqlContext(sparkContext)

    val hostAddress: InetAddress = HostAddressResolver.findHostAddress()
    logger.info("HOST ADDRESS: {}", hostAddress.getHostAddress)

    val pythonExecutionCaretaker = new PythonExecutionCaretaker(
      pythonExecutorPath,
      pythonPathGenerator,
      sparkContext,
      sqlContext,
      dataFrameStorage,
      hostAddress)

    pythonExecutionCaretaker.start()

    val executionContext = createExecutionContext(
      dataFrameStorage,
      pythonExecutionCaretaker,
      sparkContext,
      sqlContext)

    val actorSystem = ActorSystem(actorSystemName)
    val finishedExecutionStatus: Promise[ExecutionReport] = Promise()
    val statusReceiverActor =
      actorSystem.actorOf(TerminationListenerActor.props(finishedExecutionStatus))

    val workflowWithResults = WorkflowWithResults(
      workflow.id,
      workflow.metadata,
      workflow.graph,
      workflow.thirdPartyData,
      ExecutionReport(Map(), None))
    val workflowExecutorActor = actorSystem.actorOf(
      BatchWorkflowExecutorActor.props(executionContext, statusReceiverActor, workflowWithResults),
      workflow.id.toString)

    workflowExecutorActor ! Launch(workflow.graph.nodes.map(_.id))

    logger.debug("Awaiting execution end...")
    actorSystem.awaitTermination()

    val report: ExecutionReport = finishedExecutionStatus.future.value.get match {
      case Failure(exception) => // WEA failed with an exception
        logger.error("WorkflowExecutorActor failed: ", exception)
        throw exception
      case Success(executionReport: ExecutionReport) =>
        logger.debug(s"WorkflowExecutorActor finished successfully: ${workflow.graph}")
        executionReport
    }

    cleanup(actorSystem, executionContext, pythonExecutionCaretaker)
    report
  }

  private def cleanup(
      actorSystem: ActorSystem,
      executionContext: CommonExecutionContext,
      pythonExecutionCaretaker: PythonExecutionCaretaker): Unit = {
    logger.debug("Cleaning up...")
    pythonExecutionCaretaker.stop()
    logger.debug("PythonExecutionCaretaker terminated!")
    actorSystem.shutdown()
    logger.debug("Akka terminated!")
    executionContext.sparkContext.stop()
    logger.debug("Spark terminated!")
  }
}

object WorkflowExecutor extends Logging {

  private val outputFile = "result.json"

  def runInNoninteractiveMode(
      params: ExecutionParams,
      pythonPathGenerator: PythonPathGenerator): Unit = {
    val workflow = loadWorkflow(params)

    val executionReport = workflow.map(w => {
      executeWorkflow(w, params.pyExecutorPath.get, pythonPathGenerator)
    })
    val workflowWithResultsFuture = workflow.flatMap(w =>
      executionReport
        .map {
          case Success(r) => WorkflowWithResults(w.id, w.metadata, w.graph, w.thirdPartyData, r)
          case Failure(ex) => throw ex;
        }
    )

    // Await for workflow execution
    val workflowWithResultsTry = Await.ready(workflowWithResultsFuture, Duration.Inf).value.get

    workflowWithResultsTry match {
      // Workflow execution failed
      case Failure(exception) => exception match {
        case e: WorkflowVersionException => handleVersionException(e)
        case e: DeserializationException => handleDeserializationException(e)
        case e: WorkflowExecutionException => logger.error(e.getMessage, e)
        case e: Exception => logger.error("Unexpected workflow execution exception", e)
      }
      // Workflow execution succeeded
      case Success(workflowWithResults) =>
        logger.info("Handling execution report")
        // Saving execution report to file
        val reportPathFuture: Future[Option[String]] = params.outputDirectoryPath match {
          case None => Future.successful(None)
          case Some(path) => saveWorkflowToFile(path, workflowWithResults)
        }

        val reportPathTry: Try[Option[String]] =
          try {
            Await.ready(reportPathFuture, 1.minute).value.get
          } catch {
            case e: Exception =>
              executionReportDump(workflowWithResults)
              throw e
          }

        if (reportPathTry.isFailure) {
          executionReportDump(workflowWithResults)
        }

        reportPathTry match {
          case Success(None) => // Saving execution report to file was not requested
          case Success(Some(path)) =>
            logger.info(s"Execution report successfully saved to file under path: $path")
          case Failure(exception) => exception match {
            case e: WorkflowVersionException => handleVersionException(e)
            case e: DeserializationException => handleDeserializationException(e)
            case e: UnexpectedHttpResponseException => logger.error(e.getMessage)
            case e: Exception => logger.error("Saving execution report to file failed", e)
          }
        }
    }
  }

  private def executionReportDump(workflowWithResults: WorkflowWithResults): Unit = {
    logger.error("Execution report dump: \n" + workflowWithResults.toJson.prettyPrint)
  }

  private def handleVersionException(versionException: WorkflowVersionException): Unit = {
    versionException match {
      case e @ WorkflowVersionFormatException(stringVersion) =>
        logger.error(e.getMessage)
      case WorkflowVersionNotFoundException(supportedApiVersion) =>
        logger.error("The input workflow does not contain version identifier. Unable to proceed...")
      case WorkflowVersionNotSupportedException(workflowApiVersion, supportedApiVersion) =>
        logger.error(
          "The input workflow is incompatible with this WorkflowExecutor. " +
            s"Workflow's version is '${workflowApiVersion.humanReadable}' but " +
            s"WorkflowExecutor's version is '${supportedApiVersion.humanReadable}'.")
    }
  }

  private def handleDeserializationException(exception: DeserializationException): Unit = {
    logger.error(s"WorkflowExecutor is unable to parse the input file: ${exception.getMessage}")
  }

  private def executeWorkflow(
      workflow: WorkflowWithVariables,
      pythonExecutorPath: String,
      pythonPathGenerator: PythonPathGenerator): Try[ExecutionReport] = {

    // Run executor
    logger.info("Executing the workflow.")
    logger.debug("Executing the workflow: " +  workflow)
    WorkflowExecutor(workflow, pythonExecutorPath, pythonPathGenerator).execute()
  }

  private def loadWorkflow(params: ExecutionParams): Future[WorkflowWithVariables] = {
    val content = Future(Source.fromFile(params.workflowFilename.get).mkString)
    content.map(_.parseJson)
      .map(w => WorkflowJsonParamsOverrider.overrideParams(w, params.extraVars))
      .map(_.convertTo[WorkflowWithVariables](versionedWorkflowWithVariablesReader))
  }

  private def saveWorkflowToFile(
      outputDir: String,
      result: WorkflowWithResults): Future[Option[String]] = {
    logger.info(s"Execution report file ($outputFile) will be written on host: " +
      s"${InetAddress.getLocalHost.getHostName} (${InetAddress.getLocalHost.getHostAddress})")
    var writerOption: Option[PrintWriter] = None
    try {
      val resultsFile = new File(outputDir, outputFile)
      val parentFile = resultsFile.getParentFile
      if (parentFile != null) {
        parentFile.mkdirs()
      }
      logger.info(s"Writing execution report file to: ${resultsFile.getPath}")
      writerOption = Some(new PrintWriter(new FileWriter(resultsFile, false)))
      writerOption.get.write(result.toJson.prettyPrint)
      writerOption.get.flush()
      writerOption.get.close()
      Future.successful(Some(resultsFile.getPath))
    } catch {
      case e: Exception =>
        writerOption.foreach {
          writer =>
            try {
              writer.close()
            } catch {
              case e: Exception =>
                logger.warn("Exception during emergency closing of PrintWriter", e)
            }
        }
        Future.failed(e)
    }
  }
}

private case class FileSystemClientStub() extends FileSystemClient {
  override def copyLocalFile[T <: Serializable]
  (localFilePath: String, remoteFilePath: String): Unit = ()

  override def delete(path: String): Unit = ()

  override def saveObjectToFile[T <: Serializable](path: String, instance: T): Unit = ()

  override def fileExists(path: String): Boolean = throw new UnsupportedOperationException

  override def saveInputStreamToFile(
    inputStream: InputStream, destinationPath: String): Unit = ()

  override def getFileInfo(path: String): Option[FileInfo] = throw new UnsupportedOperationException

  override def readFileAsObject[T <: Serializable](path: String): T =
    throw new UnsupportedOperationException
}
