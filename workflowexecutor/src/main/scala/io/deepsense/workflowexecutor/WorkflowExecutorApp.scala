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

package io.deepsense.workflowexecutor

import java.io.{File, FileWriter, PrintWriter}
import java.net.InetAddress

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.util.{Failure, Success, Try}

import com.typesafe.config.ConfigFactory
import org.apache.log4j.xml.DOMConfigurator
import scopt.OptionParser
import spray.json._

import io.deepsense.commons.BuildInfo
import io.deepsense.commons.utils.{Logging, Version}
import io.deepsense.deeplang.CatalogRecorder
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.deeplang.doperables.ReportLevel
import io.deepsense.deeplang.doperables.ReportLevel.ReportLevel
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow._
import io.deepsense.models.json.workflow.exceptions._
import io.deepsense.models.workflows._
import io.deepsense.workflowexecutor.exception.{UnexpectedHttpResponseException, WorkflowExecutionException}

/**
 * WorkflowExecutor
 * workflow file name has to be passed via command-line parameter
 */
object WorkflowExecutorApp
  extends Logging
  with WorkflowVersionUtil {

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

  override val graphReader = new GraphReader(dOperationsCatalog())

  private val outputFile = "result.json"

  private val parser: OptionParser[ExecutionParams]
      = new scopt.OptionParser[ExecutionParams](BuildInfo.name) {
    head(BuildInfo.toString)

    note("Workflow input:")
    opt[String]('w', "workflow-filename") valueName "FILENAME" action {
      (x, c) => c.copy(workflowFilename = Some(x))
    } text "workflow filename"

    opt[String]('d', "download-workflow") valueName "ID" action {
      (x, c) => c.copy(workflowId = Some(x))
    } text "download workflow; workflow with passed ID will be downloaded from Seahorse Editor"

    note("")
    note("Execution report output:")
    opt[String]('o', "output-directory") valueName "DIR" action {
      (x, c) => c.copy(outputDirectoryPath = Some(x))
    } text
      "output directory path; directory will be created if it does not exists"

    opt[Unit]('u', "upload-report") action {
      (_, c) => c.copy(uploadReport = true)
    } text "upload execution report to Seahorse Editor"

    note("")
    note("Miscellaneous:")
    opt[String]('r', "report-level") valueName "LEVEL" action {
      (x, c) => c.copy(reportLevel = ReportLevel.withName(x.toUpperCase))
    } text "level of details for DataFrame report generation; " +
      "LEVEL is 'high', 'medium', or 'low' (default: 'medium')"

    opt[String]('a', "api-address") valueName "ADDRESS" action {
      (x, c) => c.copy(apiAddress = Some(x))
    } text "address of Seahorse Editor API. e.g. https://editor.seahorse.deepsense.io:9080"

    help("help") text "print this help message and exit"
    version("version") text "print product version and exit"
    note("")
    note("Visit https://seahorse.deepsense.io for more details")

    checkConfig { c =>
      if (c.workflowFilename.isEmpty && c.workflowId.isEmpty) {
        failure("one of --workflow-filename or --download-workflow is required")
      } else if (c.outputDirectoryPath.isEmpty && !c.uploadReport) {
        failure("one of --output-directory or --upload-report is required")
      } else {
        success
      }
    }
  }

  override def currentVersion: Version =
    Version(BuildInfo.apiVersionMajor, BuildInfo.apiVersionMinor, BuildInfo.apiVersionPatch)

  def main(args: Array[String]): Unit = {
    configureLogging()

    val cmdParams = parser.parse(args, ExecutionParams())
    if (cmdParams.isEmpty) {
      System.exit(1)
    }
    val params = cmdParams.get

    val workflow = loadWorkflow(params)
    val executionReport = workflow.map(w => executeWorkflow(w, params.reportLevel).get)
    val workflowWithResultsFuture = workflow.flatMap(w =>
      executionReport.map(r => WorkflowWithResults(w.id, w.metadata, w.graph, w.thirdPartyData, r))
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

  def configureLogging(): Unit = {
    Option(System.getProperty("logFile"))
      .getOrElse(System.setProperty("logFile", "workflowexecutor"))
    DOMConfigurator.configure(getClass.getResource("/log4j.xml"))
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

  private def dOperationsCatalog(): DOperationsCatalog = {
    val catalog = DOperationsCatalog()
    CatalogRecorder.registerDOperations(catalog)
    catalog
  }

  private case class ExecutionParams(
    workflowFilename: Option[String] = None,
    workflowId: Option[String] = None,
    outputDirectoryPath: Option[String] = None,
    uploadReport: Boolean = false,
    reportLevel: ReportLevel = ReportLevel.MEDIUM,
    apiAddress: Option[String] = None)

  private case class WorkflowManagerConfig(
      defaultAddress: String,
      path: String,
      timeout: Int) {

    def address(address: Option[String]): String = address.getOrElse(defaultAddress)
  }

  private case class ReportPreviewConfig(defaultAddress: String, path: String)
}
