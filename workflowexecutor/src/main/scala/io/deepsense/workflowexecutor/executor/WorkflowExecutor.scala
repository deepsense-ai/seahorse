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
import com.typesafe.config.ConfigFactory
import spray.json._

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.ReportLevel.ReportLevel
import io.deepsense.models.entities.Entity
import io.deepsense.models.json.workflow.exceptions.{WorkflowVersionException, WorkflowVersionFormatException, WorkflowVersionNotFoundException, WorkflowVersionNotSupportedException}
import io.deepsense.models.workflows.{ExecutionReport, WorkflowWithResults, WorkflowWithVariables}
import io.deepsense.workflowexecutor.WorkflowExecutorActor.Messages.{GraphFinished, Launch}
import io.deepsense.workflowexecutor.WorkflowExecutorApp._
import io.deepsense.workflowexecutor.exception.{UnexpectedHttpResponseException, WorkflowExecutionException}
import io.deepsense.workflowexecutor.{ExecutionParams, ReportUploadClient, WorkflowDownloadClient, WorkflowExecutorActor}

/**
 * WorkflowExecutor creates an execution context and then executes a workflow on Spark.
 */
case class WorkflowExecutor(
    workflow: WorkflowWithVariables,
    reportLevel: ReportLevel)
  extends Executor {

  val dOperableCache = mutable.Map[Entity.Id, DOperable]()
  private val actorSystemName = "WorkflowExecutor"

  def execute(): Try[ExecutionReport] = {
    val executionContext = createExecutionContext(reportLevel)

    val actorSystem = ActorSystem(actorSystemName)
    val workflowExecutorActor = actorSystem.actorOf(WorkflowExecutorActor.props(executionContext))

    val startedTime = DateTimeConverter.now

    val resultPromise: Promise[GraphFinished] = Promise()
    workflowExecutorActor ! Launch(workflow.graph, resultPromise)

    logger.debug("Awaiting execution end...")
    actorSystem.awaitTermination()

    val report = resultPromise.future.value.get match {
      case Failure(exception) => // WEA failed with an exception
        logger.error("WorkflowExecutorActor failed: ", exception)
        throw exception
      case Success(GraphFinished(graph, entitiesMap)) =>
        logger.debug(s"WorkflowExecutorActor finished successfully: ${workflow.graph}")
        Try(ExecutionReport(
          graph.state,
          startedTime,
          DateTimeConverter.now,
          graph.states,
          entitiesMap
        ))
    }

    cleanup(actorSystem, executionContext)
    report
  }

  private def cleanup(actorSystem: ActorSystem, executionContext: ExecutionContext): Unit = {
    logger.debug("Cleaning up...")
    actorSystem.shutdown()
    logger.debug("Akka terminated!")
    executionContext.sparkContext.stop()
    logger.debug("Spark terminated!")
  }
}

object WorkflowExecutor extends Logging {

  private val config = ConfigFactory.load

  private val workflowManagerConfig = WorkflowManagerConfig(
    defaultAddress = config.getString("workflow-manager.address"),
    path = config.getString("workflow-manager.path"),
    timeout = config.getInt("workflow-manager.timeout")
  )

  private val reportPreviewConfig = ReportPreviewConfig(
    defaultAddress = config.getString("editor.address"),
    path = config.getString("editor.report-preview.path")
  )

  private val outputFile = "result.json"


  def runInNoninteractiveMode(params: ExecutionParams): Unit = {
    val workflow = loadWorkflow(params)

    val executionReport = workflow.map(w => {
      val workflowWithExtraVars = substituteExtraVars(w, params.extraVars)
      executeWorkflow(workflowWithExtraVars, params.reportLevel).get
    })
    val workflowWithResultsFuture = workflow.flatMap(w =>
      executionReport
        .map(r => WorkflowWithResults(w.id, w.metadata, w.graph, w.thirdPartyData, r))
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
      // Workflow excecution succeeded
      case Success(workflowWithResults) => {
        logger.info("Handling execution report")
        // Uploading execution report
        val reportUrlFuture: Future[Option[String]] = params.uploadReport match {
          case false => Future.successful(None)
          case true =>
            val reportUploadAddress = workflowManagerConfig.address(params.apiAddress)
            val reportPreviewAddress = reportPreviewConfig.defaultAddress
            uploadExecutionReport(reportUploadAddress, reportPreviewAddress, workflowWithResults)
              .map(Some(_))
        }
        // Saving execution report to file
        val reportPathFuture: Future[Option[String]] = params.outputDirectoryPath match {
          case None => Future.successful(None)
          case Some(path) => saveWorkflowToFile(path, workflowWithResults)
        }

        val reportUrlTry = Await.ready(reportUrlFuture, 1.minute).value.get
        val reportPathTry = Await.ready(reportPathFuture, 1.minute).value.get

        if (reportUrlTry.isFailure || reportPathTry.isFailure) {
          logger.error("Execution report dump: \n" + workflowWithResults.toJson.prettyPrint)
        }

        reportUrlTry match {
          case Success(None) => // Uploading execution report was not requested
          case Success(Some(url)) =>
            logger.info(s"Exceution report successfully uploaded to: $url")
          case Failure(exception) => exception match {
            case e: WorkflowVersionException => handleVersionException(e)
            case e: DeserializationException => handleDeserializationException(e)
            case e: UnexpectedHttpResponseException => logger.error(e.getMessage)
            case e: Exception => logger.error("Uploading exceution report failed", e)
          }
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
  }


  private def reportUrl(reportId: String): String =
    s"${reportPreviewConfig.defaultAddress}/${reportPreviewConfig.path}/$reportId"

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

  private def substituteExtraVars(
      workflow: WorkflowWithVariables,
      extraVars: Map[String, String]): WorkflowWithVariables = {

    val workflowCopy = workflow.toJson
      .convertTo[WorkflowWithVariables](versionedWorkflowWithVariablesReader)
    workflowCopy.graph.nodes.foreach(node =>
      node.operation.parameters.substitutePlaceholders(extraVars)
    )
    workflowCopy
  }

  private def executeWorkflow(
      workflow: WorkflowWithVariables,
      reportLevel: ReportLevel): Try[ExecutionReport] = {

    // Run executor
    logger.info("Executing the workflow.")
    logger.debug("Executing the workflow: " +  workflow)
    WorkflowExecutor(workflow, reportLevel).execute()
  }

  private def loadWorkflow(params: ExecutionParams): Future[WorkflowWithVariables] = {
    val content = params.workflowId match {
      case Some(id) =>
        val editorAddress = workflowManagerConfig.address(params.apiAddress)
        downloadWorkflow(editorAddress, id)
      case None =>
        Future(Source.fromFile(params.workflowFilename.get).mkString)
    }

    content.map(_.parseJson.convertTo[WorkflowWithVariables](versionedWorkflowWithVariablesReader))
  }

  private def downloadWorkflow(editorAddress: String, workflowId: String): Future[String] = {
    new WorkflowDownloadClient(
      editorAddress,
      workflowManagerConfig.path,
      workflowManagerConfig.timeout
    ).downloadWorkflow(workflowId)
  }

  private def uploadExecutionReport(
      reportUploadAddress: String,
      reportPreviewAddress: String,
      result: WorkflowWithResults): Future[String] = {
    new ReportUploadClient(
      reportUploadAddress,
      workflowManagerConfig.path,
      workflowManagerConfig.timeout,
      graphReader
    ).uploadReport(result)
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
      case e =>
        writerOption.map {
          writer =>
            try {
              writer.close()
            } catch {
              case e =>
                logger.warn("Exception during emergency closing of PrintWriter", e)
            }
        }
        return Future.failed(e)
    }
  }

  private case class WorkflowManagerConfig(
      defaultAddress: String,
      path: String,
      timeout: Int) {

    def address(address: Option[String]): String = address.getOrElse(defaultAddress)
  }

  private case class ReportPreviewConfig(defaultAddress: String, path: String)
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
