/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.workflowexecutor.executor

import java.io._
import java.net.InetAddress

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import org.apache.spark.SparkContext
import spray.json._

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}
import scala.io.Source
import scala.util.{Failure, Success, Try}

import ai.deepsense.commons.json.datasources.DatasourceListJsonProtocol
import ai.deepsense.commons.models.Entity
import ai.deepsense.commons.rest.client.datasources.DatasourceInMemoryClientFactory
import ai.deepsense.commons.rest.client.datasources.DatasourceTypes.DatasourceList
import ai.deepsense.commons.utils.{Logging, Version}
import ai.deepsense.deeplang.{OperationExecutionDispatcher, _}
import ai.deepsense.graph.CyclicGraphException
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import ai.deepsense.models.json.workflow.WorkflowVersionUtil
import ai.deepsense.models.json.workflow.exceptions._
import ai.deepsense.models.workflows.{ExecutionReport, WorkflowInfo, WorkflowWithResults, WorkflowWithVariables}
import ai.deepsense.sparkutils.AkkaUtils
import ai.deepsense.workflowexecutor.WorkflowExecutorActor.Messages.Launch
import ai.deepsense.workflowexecutor._
import ai.deepsense.workflowexecutor.buildinfo.BuildInfo
import ai.deepsense.workflowexecutor.customcode.CustomCodeEntryPoint
import ai.deepsense.workflowexecutor.exception.{UnexpectedHttpResponseException, WorkflowExecutionException}
import ai.deepsense.workflowexecutor.pyspark.PythonPathGenerator
import ai.deepsense.workflowexecutor.session.storage.DataFrameStorageImpl

/**
 * WorkflowExecutor creates an execution context and then executes a workflow on Spark.
 */
case class WorkflowExecutor(
    workflow: WorkflowWithVariables,
    customCodeExecutorsPath: String,
    pythonPathGenerator: PythonPathGenerator,
    tempPath: String)
  extends Executor {

  val dOperableCache = mutable.Map[Entity.Id, DOperable]()
  private val actorSystemName = "WorkflowExecutor"

  def execute(sparkContext: SparkContext): Try[ExecutionReport] = Try {

    if (workflow.graph.containsCycle) {
      val cyclicGraphException = new CyclicGraphException
      logger.error("WorkflowExecutorActor failed due to incorrect workflow: ", cyclicGraphException)
      throw cyclicGraphException
    }

    val dataFrameStorage = new DataFrameStorageImpl

    val sparkSQLSession = createSparkSQLSession(sparkContext)

    val hostAddress: InetAddress = HostAddressResolver.findHostAddress()
    logger.info("HOST ADDRESS: {}", hostAddress.getHostAddress)

    val pythonBinary = ConfigFactory.load
        .getString("pythoncaretaker.python-binary-default")

    val operationExecutionDispatcher = new OperationExecutionDispatcher

    val customCodeEntryPoint = new CustomCodeEntryPoint(
      sparkContext,
      sparkSQLSession,
      dataFrameStorage,
      operationExecutionDispatcher)

    val pythonExecutionCaretaker = new PythonExecutionCaretaker(
      customCodeExecutorsPath,
      pythonPathGenerator,
      pythonBinary,
      sparkContext,
      sparkSQLSession,
      dataFrameStorage,
      customCodeEntryPoint,
      hostAddress)
    pythonExecutionCaretaker.start()

    val rExecutionCaretaker = new RExecutionCaretaker(customCodeExecutorsPath, customCodeEntryPoint)
    rExecutionCaretaker.start()

    val customCodeExecutionProvider = CustomCodeExecutionProvider(
      pythonExecutionCaretaker.pythonCodeExecutor,
      rExecutionCaretaker.rCodeExecutor,
      operationExecutionDispatcher)

    val libraryPath = "/library"

    val datasources = WorkflowExecutor.datasourcesFrom(workflow)

    val executionContext = createExecutionContext(
      dataFrameStorage = dataFrameStorage,
      executionMode = ExecutionMode.Batch,
      notebooksClientFactory = None,
      emailSender = None,
      datasourceClientFactory = new DatasourceInMemoryClientFactory(datasources),
      customCodeExecutionProvider = customCodeExecutionProvider,
      sparkContext = sparkContext,
      sparkSQLSession = sparkSQLSession,
      tempPath = tempPath,
      libraryPath = libraryPath
    )

    val actorSystem = ActorSystem(actorSystemName)
    val finishedExecutionStatus: Promise[ExecutionReport] = Promise()
    val statusReceiverActor =
      actorSystem.actorOf(TerminationListenerActor.props(finishedExecutionStatus))

    val workflowWithResults = WorkflowWithResults(
      workflow.id,
      workflow.metadata,
      workflow.graph,
      workflow.thirdPartyData,
      ExecutionReport(Map(), None),
      WorkflowInfo.forId(workflow.id))
    val workflowExecutorActor = actorSystem.actorOf(
      BatchWorkflowExecutorActor.props(executionContext, statusReceiverActor, workflowWithResults),
      workflow.id.toString)

    workflowExecutorActor ! Launch(workflow.graph.nodes.map(_.id))

    logger.debug("Awaiting execution end...")
    AkkaUtils.awaitTermination(actorSystem)

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
    AkkaUtils.terminate(actorSystem)
    logger.debug("Akka terminated!")
    executionContext.sparkContext.stop()
    logger.debug("Spark terminated!")
  }
}

object WorkflowExecutor extends Logging with Executor {

  private val outputFile = "result.json"

  def datasourcesFrom(workflow: WorkflowWithVariables): DatasourceList = {
    val datasourcesJson = workflow.thirdPartyData.fields("datasources")
    val datasourcesJsonString = datasourcesJson.compactPrint
    DatasourceListJsonProtocol.fromString(datasourcesJsonString)
  }

  def runInNoninteractiveMode(
      params: ExecutionParams,
      pythonPathGenerator: PythonPathGenerator): Unit = {
    val sparkContext = createSparkContext()
    val dOperationsCatalog = CatalogRecorder.fromSparkContext(sparkContext).catalogs.operations

    val workflowVersionUtil: WorkflowVersionUtil = new WorkflowVersionUtil with Logging {
      override def currentVersion: Version =
        Version(BuildInfo.apiVersionMajor, BuildInfo.apiVersionMinor, BuildInfo.apiVersionPatch)

      override protected val graphReader: GraphReader = new GraphReader(dOperationsCatalog)
    }
    val workflow = loadWorkflow(params, workflowVersionUtil)

    val executionReport = workflow.map(w => {executeWorkflow(w, params.customCodeExecutorsPath.get,
      pythonPathGenerator, params.tempPath.get, sparkContext)
    })
    val workflowWithResultsFuture = workflow.flatMap(w =>
      executionReport
        .map {
          case Success(r) =>
            val emptyWorkflowInfo = WorkflowInfo.forId(w.id)
            WorkflowWithResults(w.id, w.metadata, w.graph, w.thirdPartyData, r, emptyWorkflowInfo)
          case Failure(ex) =>
            logger.error(s"Error while processing workflow: $workflow")
            throw ex
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
          case Some(path) => saveWorkflowToFile(path, workflowWithResults, workflowVersionUtil)
        }

        val reportPathTry: Try[Option[String]] =
          try {
            Await.ready(reportPathFuture, 1.minute).value.get
          } catch {
            case e: Exception =>
              executionReportDump(workflowWithResults, workflowVersionUtil)
              throw e
          }

        if (reportPathTry.isFailure) {
          executionReportDump(workflowWithResults, workflowVersionUtil)
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

  private def executionReportDump(
      workflowWithResults: WorkflowWithResults,
      workflowVersionUtil: WorkflowVersionUtil): Unit = {
    import workflowVersionUtil._
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
      customCodeExecutorsPath: String,
      pythonPathGenerator: PythonPathGenerator,
      tempPath: String,
      sparkContext: SparkContext): Try[ExecutionReport] = {

    // Run executor
    logger.info("Executing the workflow.")
    logger.debug("Executing the workflow: " +  workflow)
    WorkflowExecutor(workflow, customCodeExecutorsPath, pythonPathGenerator, tempPath).execute(sparkContext)
  }

  private def loadWorkflow(params: ExecutionParams,
      workflowVersionUtil: WorkflowVersionUtil): Future[WorkflowWithVariables] = {
    val content = Future(Source.fromFile(params.workflowFilename.get).mkString)
    content.map(_.parseJson)
      .map(w => WorkflowJsonParamsOverrider.overrideParams(w, params.extraVars))
      .map(_.convertTo[WorkflowWithVariables](
        workflowVersionUtil.versionedWorkflowWithVariablesReader))
  }

  private def saveWorkflowToFile(
      outputDir: String,
      result: WorkflowWithResults,
      workflowVersionUtil: WorkflowVersionUtil): Future[Option[String]] = {
    import workflowVersionUtil._
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
