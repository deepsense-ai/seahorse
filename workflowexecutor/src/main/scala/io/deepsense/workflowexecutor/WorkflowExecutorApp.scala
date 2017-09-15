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

import org.apache.log4j.xml.DOMConfigurator
import scopt.OptionParser

import io.deepsense.commons.BuildInfo
import io.deepsense.commons.utils.{Logging, Version}
import io.deepsense.deeplang.CatalogRecorder
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow._
import io.deepsense.workflowexecutor.executor.{SessionExecutor, WorkflowExecutor}

/**
 * WorkflowExecutor
 * workflow file name has to be passed via command-line parameter
 */
object WorkflowExecutorApp extends Logging with WorkflowVersionUtil {

  override val graphReader = new GraphReader(dOperationsCatalog())

  private val parser: OptionParser[ExecutionParams]
      = new scopt.OptionParser[ExecutionParams](BuildInfo.name) {
    head(BuildInfo.toString)

    // Hidden option: Running modes:
    opt[Unit]("interactive-mode") hidden() action {
      (_, c) => c.copy(interactiveMode = true)
    } text "use interactive mode (used only in Seahorse Desktop)"

    note("Workflow input:")
    opt[String]('w', "workflow-filename") valueName "FILENAME" action {
      (x, c) => c.copy(workflowFilename = Some(x))
    } text "workflow filename"

    note("")
    note("Execution report output:")
    opt[String]('o', "output-directory") valueName "DIR" action {
      (x, c) => c.copy(outputDirectoryPath = Some(x))
    } text
      "output directory path; directory will be created if it does not exists"

    note("")
    note("Miscellaneous:")

    opt[(String, String)]('e', "extra-var") optional() unbounded() valueName "NAME=VALUE" action {
      (x, c) => c.copy(extraVars = c.extraVars.updated(x._1, x._2))
    } text "extra variable; can be specified multiple times; " +
      "name or value can be surrounded by quotation marks " +
      "if it contains special characters (e.g. space)"

    // Hidden option:
    opt[String]('m', "message-queue-host") hidden() valueName "HOST" action {
      (x, c) => c.copy(messageQueueHost = Some(x))
    } text "message queue host"

    // Hidden option:
    opt[String]('j', "job-id") hidden() valueName "JOB" action {
      (x, c) => c.copy(jobId = Some(x))
    } text "job id"

    opt[String]('p', "python-executor-path") required() valueName "PATH" action {
      (x, c) => c.copy(pyExecutorPath = Some(x))
    } text "PyExecutor code (included in workflowexecutor.jar) path"

    help("help") text "print this help message and exit"
    version("version") text "print product version and exit"
    note("")

    note("Visit https://seahorse.deepsense.io for more details")

    checkConfig { config =>
      type FailureCondition = Boolean
      type ErrorMsg = String
      type Requirements = Seq[(FailureCondition, ErrorMsg)]

      val interactiveRequirements: Requirements = Seq(
        (config.messageQueueHost.isEmpty, "--message-queue-host is required in interactive mode"),
        (config.jobId.isEmpty, "--job-id is required in interactive mode")
      )

      val nonInteractiveRequirements: Requirements = Seq(
        (config.workflowFilename.isEmpty,
          "--workflow-filename is required by Seahorse Batch Workflow Executor"),
        (config.outputDirectoryPath.isEmpty,
          "--output-directory is required by Seahorse Batch Workflow Executor"))

      def check(requirements: Requirements) = {
        requirements.foldLeft(success) {
          case (f@Left(_), _) => f
          case (_, (true, msg)) => failure(msg)
          case (_, (false, _)) => success
        }
      }

      if (config.interactiveMode) {
        check(interactiveRequirements)
      } else {
        check(nonInteractiveRequirements)
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

    if (params.interactiveMode) {
      // Interactive mode (SessionExecutor)
      logger.info("Starting SessionExecutor.")
      logger.debug("Starting SessionExecutor.")
      SessionExecutor(
        params.messageQueueHost.get,
        params.pyExecutorPath.get,
        params.jobId.get
      ).execute()
    } else {
      // Running in non-interactive mode
      WorkflowExecutor.runInNoninteractiveMode(params)
    }
  }

  def configureLogging(): Unit = {
    Option(System.getProperty("logFile"))
      .getOrElse(System.setProperty("logFile", "workflowexecutor"))
    DOMConfigurator.configure(getClass.getResource("/log4j.xml"))
  }

  private def dOperationsCatalog(): DOperationsCatalog = {
    val catalog = DOperationsCatalog()
    CatalogRecorder.registerDOperations(catalog)
    catalog
  }
}
