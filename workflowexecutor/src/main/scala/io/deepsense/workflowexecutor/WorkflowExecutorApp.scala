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

import scala.io.Source
import scala.util.{Try, Failure, Success}

import buildinfo.BuildInfo
import scopt.OptionParser
import spray.json._

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.CatalogRecorder
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow.{WorkflowWithResultsJsonProtocol, WorkflowWithVariablesJsonProtocol}
import io.deepsense.models.workflows._


/**
 * WorkflowExecutor
 * workflow file name has to be passed via command-line parameter
 */
object WorkflowExecutorApp
  extends Logging
  with WorkflowWithVariablesJsonProtocol
  with WorkflowWithResultsJsonProtocol {

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
        handleExecutionReport(params.outputDirectoryPath, executionReport, workflowWithVariables)
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
      workflow: WorkflowWithVariables): Unit = {

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

  private def dOperationsCatalog(): DOperationsCatalog = {
    val catalog = DOperationsCatalog()
    CatalogRecorder.registerDOperations(catalog)
    catalog
  }

  private case class ExecutionParams(
    workflowFilename: String = "",
    outputDirectoryPath: String = "",
    generateReport: Boolean = false)
}
