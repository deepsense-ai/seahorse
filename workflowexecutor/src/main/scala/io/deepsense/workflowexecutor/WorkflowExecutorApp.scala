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

    logger.info("Starting WorkflowExecutor.")

    val result = cmdParams.map { params =>
      loadWorkflow(params)
        .map(workflow => (workflow, executeWorkflow(workflow, params.reportLevel).get))
        .flatMap { case (workflow, report) => handleExecutionReport(params, report, workflow) }
    }

    result match {
      case None =>
        System.exit(1)
      case Some(res) =>
        val resultUrl = Await.ready(res, Duration.Inf).value.get
        resultUrl match {
          case Success(Some(reportId)) =>
            logger.info(s"Report uploaded. Report id: $reportId. " +
              s"You can see the results at: ${reportUrl(reportId)}, or at Your custom location.")
          case Success(None) =>
            logger.info("Workflow execution finished.")
          case Failure(exception) => exception match {
            case e: WorkflowVersionException => handleVersionException(e)
            case e: DeserializationException => handleDeserializationException(e)
            case e: WorkflowExecutionException => logger.error(e.getMessage, e)
            case e: UnexpectedHttpResponseException => logger.error(e.getMessage)
            case e: Exception => logger.error(s"Unexpected exception", e)
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

  private def handleExecutionReport(
      params: ExecutionParams,
      executionReport: ExecutionReport,
      workflow: WorkflowWithVariables): Future[Option[String]] = {

    val result = WorkflowWithResults(
      workflow.id,
      workflow.metadata,
      workflow.graph,
      workflow.thirdPartyData,
      executionReport
    )

    params.outputDirectoryPath.foreach { path =>
      saveWorkflowWithResults(path, result)
    }

    if (params.uploadReport) {
      val reportUploadAddress = workflowManagerConfig.address(params.apiAddress)
      val reportPreviewAddress = reportPreviewConfig.defaultAddress
      uploadExecutionReport(reportUploadAddress, reportPreviewAddress, result).map(Some(_))
    } else {
      Future.successful(None)
    }
  }

  private def saveWorkflowWithResults(outputDir: String, result: WorkflowWithResults): Unit = {
    val resultsFile = new File(outputDir, outputFile)
    val parentFile = resultsFile.getParentFile
    if (parentFile != null) {
      parentFile.mkdirs()
    }
    logger.info(s"Writing result to: ${resultsFile.getPath}")
    val writer = new PrintWriter(new FileWriter(resultsFile, false))
    writer.write(result.toJson.prettyPrint)
    writer.flush()
    writer.close()
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
