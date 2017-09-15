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

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.io.Source
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

import com.typesafe.config.ConfigFactory
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
    scheme = config.getString("workflow-manager.scheme"),
    defaultHost = config.getString("workflow-manager.host"),
    port = config.getInt("workflow-manager.port"),
    path = config.getString("workflow-manager.path"),
    timeout = config.getInt("workflow-manager.timeout")
  )

  private val reportPreviewConfig = ReportPreviewConfig(
    scheme = config.getString("report-preview.scheme"),
    defaultHost = config.getString("report-preview.host"),
    port = config.getInt("report-preview.port"),
    path = config.getString("report-preview.path")
  )

  override val graphReader = new GraphReader(dOperationsCatalog())

  private val outputFile = "result.json"

  private val parser: OptionParser[ExecutionParams]
      = new scopt.OptionParser[ExecutionParams](BuildInfo.name) {
    head(BuildInfo.toString)

    opt[String]('r', "report-level") valueName "LEVEL" action {
      (x, c) => c.copy(reportLevel = ReportLevel.withName(x.toUpperCase))
    } text "level of details for DataFrame report generation; " +
      "LEVEL is 'high', 'medium', or 'low' (default: 'medium')"

    opt[String]('w', "workflow-filename") valueName "FILENAME" action {
      (x, c) => c.copy(workflowFilename = Some(x))
    } text "workflow filename"

    opt[String]('o', "output-directory") valueName "DIR" action {
      (x, c) => c.copy(outputDirectoryPath = Some(x))
    } text
      "output directory path; directory will be created if it does not exists; " +
        "execution fails if any file is to be overwritten"

    opt[String]('d', "workflow-id") valueName "WORKFLOW_ID" action {
      (x, c) => c.copy(workflowId = Some(x))
    } text "workflow ID; workflow will be downloaded from Seahorse Editor"

    opt[Unit]('u', "upload-report") action {
      (_, c) => c.copy(uploadReport = true)
    } text "upload execution report to Seahorse Editor"

    opt[String]('e', "editor-host") valueName "HOST" action {
      (x, c) => c.copy(editorHost = Some(x))
    } text "hostname or IP of Seahorse Editor"

    help("help") text "print this help message and exit"
    version("version") text "print product version and exit"
    note("See http://deepsense.io for more details")

    checkConfig { c =>
      if (c.workflowFilename.isEmpty && c.workflowId.isEmpty) {
        failure("one of --workflow-filename or --workflow-id is required")
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
          case Success(Some(url)) =>
            logger.info(s"Report uploaded. You can see the results at: $url")
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
        val editorHost = workflowManagerConfig.host(params.editorHost)
        downloadWorkflow(editorHost, id)
      case None =>
        Future(Source.fromFile(params.workflowFilename.get).mkString)
    }

    content.map(_.parseJson.convertTo[WorkflowWithVariables](versionedWorkflowWithVariablesReader))
  }

  private def downloadWorkflow(editorHost: String, workflowId: String): Future[String] = {
    new WorkflowDownloadClient(
      editorHost,
      workflowManagerConfig.scheme,
      workflowManagerConfig.port,
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
      val editorHost = workflowManagerConfig.host(params.editorHost)
      uploadExecutionReport(editorHost, result).map(Some(_))
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
      reportUploadHost: String,
      result: WorkflowWithResults): Future[String] = {
    new ReportUploadClient(reportUploadHost,
      workflowManagerConfig.scheme,
      workflowManagerConfig.port,
      workflowManagerConfig.path,
      workflowManagerConfig.timeout,
      reportPreviewConfig.scheme,
      reportPreviewConfig.port,
      reportPreviewConfig.path,
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
    editorHost: Option[String] = None)

  private case class WorkflowManagerConfig(
      scheme: String,
      defaultHost: String,
      port: Int,
      path: String,
      timeout: Int) {

    def host(hostname: Option[String]): String = hostname.getOrElse(defaultHost)
  }

  private case class ReportPreviewConfig(
      scheme: String,
      defaultHost: String,
      port: Int,
      path: String) {

    def host(hostname: Option[String]): String = hostname.getOrElse(defaultHost)
  }
}
