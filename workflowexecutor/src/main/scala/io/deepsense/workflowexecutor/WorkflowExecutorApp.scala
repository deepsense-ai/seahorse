/**
 * Copyright 2015, CodiLime Inc.
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

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Source
import scala.util.{Failure, Success, Try}

import akka.actor.ActorSystem
import buildinfo.BuildInfo
import com.typesafe.config.ConfigFactory
import scopt.OptionParser
import spray.json._

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.CatalogRecorder
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow._
import io.deepsense.models.workflows._


/**
 * WorkflowExecutor
 * workflow file name has to be passed via command-line parameter
 */
object WorkflowExecutorApp
  extends Logging
  with WorkflowWithVariablesJsonProtocol
  with WorkflowWithResultsJsonProtocol
  with WorkflowWithSavedResultsJsonProtocol {

  private val config = ConfigFactory.load

  override val graphReader = new GraphReader(dOperationsCatalog())
  private val outputFile = "result.json"
  private val parser: OptionParser[ExecutionParams]
      = new scopt.OptionParser[ExecutionParams](BuildInfo.name) {
    head(BuildInfo.toString)
    opt[Unit]('r', "generate-report") action {
      (_, c) => c.copy(generateReport = true)
    } text "compute ExecutionReport (very time consuming option)"
    opt[String]('w', "workflow-filename") required() valueName "<file-name>" action {
      (x, c) => c.copy(workflowFilename = x)
    } text "workflow filename"
    opt[String]('o', "output-directory") required() valueName "<dir-path>" action {
      (x, c) => c.copy(outputDirectoryPath = x)
    } text
      "output directory path; directory will be created if it does not exists; " +
        "execution fails if any file is to be overwritten"
    opt[String]('u', "report-upload-host") valueName "<host>" action {
      (x, c) => c.copy(reportUploadHost = Some(x))
    } text "hostname or IP of the server where execution report should be uploaded"
    help("help") text "print this help message"
    version("version") text "print product version and exit"
    note("See http://deepsense.io for more details")
  }

  def main(args: Array[String]): Unit = {
    logger.info("Starting WorkflowExecutor.")
    val cmdParams = parser.parse(args, ExecutionParams())

    cmdParams match {
      case None => System.exit(-1)
      case Some(params) =>
        val workflowWithVariables = loadWorkflow(params.workflowFilename)
        val executionReport = executeWorkflow(workflowWithVariables, params.generateReport)
        handleExecutionReport(params.outputDirectoryPath, executionReport, workflowWithVariables,
          params.reportUploadHost)
    }
  }

  private def executeWorkflow(
      workflow: WorkflowWithVariables,
      generateReport: Boolean): Try[ExecutionReport] = {
    // Run executor
    logger.info("Executing the workflow.")
    logger.debug("Executing the workflow: " +  workflow)
    WorkflowExecutor(workflow, generateReport).execute()
  }

  private def loadWorkflow(filename: String): WorkflowWithVariables =
    Source.fromFile(filename)
      .mkString
      .parseJson
      .convertTo[WorkflowWithVariables]

  private def handleExecutionReport(
      outputDirectoryPath: String,
      executionReport: Try[ExecutionReport],
      workflow: WorkflowWithVariables,
      reportUploadHost: Option[String]): Unit = {

    executionReport match {
      case Failure(exception) => logger.error("Execution failed:", exception)
      case Success(value) =>
        val result = WorkflowWithResults(
          workflow.id,
          workflow.metadata,
          workflow.graph,
          workflow.thirdPartyData,
          value
        )

        saveWorkflowWithResults(outputDirectoryPath, result)
        uploadExecutionReport(reportUploadHost, result)
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
      reportUploadHost: Option[String], result: WorkflowWithResults): Unit = {

    reportUploadHost.foreach { hostname =>

      val reportUploadScheme = config.getString("report.upload.scheme")
      val reportUploadPort = config.getInt("report.upload.port")
      val reportUploadPath = config.getString("report.upload.path")
      val reportUploadTimeout = config.getInt("report.upload.timeout")

      val reportPreviewScheme = config.getString("report.preview.scheme")
      val reportPreviewPort = config.getInt("report.preview.port")
      val reportPreviewPath = config.getString("report.preview.path")

      val uploadReport = new ReportUploadClient(hostname,
        reportUploadScheme, reportUploadPort, reportUploadPath, reportUploadTimeout,
        reportPreviewScheme, reportPreviewPort, reportPreviewPath,
        graphReader).uploadReport(result)

      val resultUrl = Await.ready(uploadReport, reportUploadTimeout.seconds).value.get
      resultUrl match {
        case Success(url) =>
          logger.info(s"Report uploaded. You can see the results at: ${url}")
        case Failure(ex) =>
          logger.error(s"Report upload failed: ${ex.getMessage}.", ex)
      }
    }
  }

  private def dOperationsCatalog(): DOperationsCatalog = {
    val catalog = DOperationsCatalog()
    CatalogRecorder.registerDOperations(catalog)
    catalog
  }

  private case class ExecutionParams(
    workflowFilename: String = "",
    outputDirectoryPath: String = "",
    generateReport: Boolean = false,
    reportUploadHost: Option[String] = None)
}
