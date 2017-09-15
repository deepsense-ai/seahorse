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

import io.deepsense.commons.utils.{Logging, Version}
import io.deepsense.deeplang.CatalogRecorder
import io.deepsense.deeplang.catalogs.CatalogPair
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow._
import io.deepsense.workflowexecutor.buildinfo.BuildInfo
import io.deepsense.workflowexecutor.executor.{SessionExecutor, WorkflowExecutor}
import io.deepsense.workflowexecutor.pyspark.PythonPathGenerator

/**
 * WorkflowExecutor
 * workflow file name has to be passed via command-line parameter
 */
object WorkflowExecutorApp extends Logging with WorkflowVersionUtil {

  val CatalogPair(_, dOperationsCatalog) = CatalogRecorder.createCatalogs()
  override val graphReader = new GraphReader(dOperationsCatalog)

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
    opt[Int]("message-queue-port") hidden() valueName "PORT" action {
      (x, c) => c.copy(messageQueuePort = Some(x))
    } text "message queue port"

    // Hidden option:
    opt[String]('m', "message-queue-user") hidden() valueName "USER" action {
      (x, c) => c.copy(messageQueueUser = Some(x))
    } text "message queue user"

    // Hidden option:
    opt[String]("message-queue-pass") hidden() valueName "PASS" action {
      (x, c) => c.copy(messageQueuePass = Some(x))
    } text "message queue pass"

    // Hidden option:
    opt[String]("workflow-id") hidden() valueName "JOB" action {
      (x, c) => c.copy(workflowId = Some(x))
    } text "job id"

    opt[String]("wm-address") hidden() valueName "URL" action {
      (x, c) => c.copy(wmAddress = Some(x))
    } text "workflow Manager address"

    opt[String]('d', "deps-zip") hidden() valueName "FILE" action {
      (x, c) => c.copy(depsZip = Some(x))
    } text "dependencies zip file"

    opt[String]('u', "user-id") hidden() valueName "USER_ID" action {
      (x, c) => c.copy(userId = Some(x))
    } text "id of the workflow's owner"

    opt[String]( "wm-username") hidden() valueName "USER" action {
      (x, c) => c.copy(wmUsername = Some(x))
    } text "user for accessing Workflow Manager API"

    opt[String]( "wm-password") hidden() valueName "PASSWORD" action {
      (x, c) => c.copy(wmPassword = Some(x))
    } text "password for accessing Workflow Manager API"

    opt[String]('x', "custom-code-executors-path") optional() valueName "PATH" action {
      (x, c) => c.copy(customCodeExecutorsPath = Some(x))
    } text "Custom code executors (included in workflowexecutor.jar) path"

    opt[String]("python-binary") optional() valueName "PATH" action {
      (x, c) => c.copy(pythonBinaryPath = Some(x))
    } text "Python binary path"

    opt[String]('t', "temp-dir") optional() valueName "PATH" action {
      (x, c) => c.copy(tempPath = Some(x))
    } text "Temporary directory path"

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
        (config.messageQueuePort.isEmpty, "--message-queue-port is required in interactive mode"),
        (config.messageQueueUser.isEmpty, "--message-queue-user is required in interactive mode"),
        (config.messageQueuePass.isEmpty, "--message-queue-pass is required in interactive mode"),
        (config.workflowId.isEmpty, "--workflow-id is required in interactive mode"),
        (config.wmAddress.isEmpty, "--wm-address is required in interactive mode"),
        (config.depsZip.isEmpty, "--deps-zip is required in interactive mode"),
        (config.wmUsername.isEmpty, "--wm-username is required in interactive mode"),
        (config.wmPassword.isEmpty, "--wm-password is required in interactive mode"),
        (config.userId.isEmpty, "--user-id is required in interactive mode"),
        (config.tempPath.isEmpty, "--temp-dir is required in interactive mode"),
        (config.customCodeExecutorsPath.isDefined,
          "--custom-code-executors-path is forbidden in interactive mode")
      )

      val nonInteractiveRequirements: Requirements = Seq(
        (config.workflowFilename.isEmpty,
          "--workflow-filename is required by Seahorse Batch Workflow Executor"),
        (config.outputDirectoryPath.isEmpty,
          "--output-directory is required by Seahorse Batch Workflow Executor"),
        (config.customCodeExecutorsPath.isEmpty,
          "--custom-code-executors-path is required by Seahorse Batch Workflow Executor"))

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
      SessionExecutor(
        params.messageQueueHost.get,
        params.messageQueuePort.get,
        params.messageQueueUser.get,
        params.messageQueuePass.get,
        params.workflowId.get,
        params.wmAddress.get,
        params.wmUsername.get,
        params.wmPassword.get,
        params.depsZip.get,
        params.userId.get,
        params.tempPath.get,
        params.pythonBinaryPath
      ).execute()
    } else {
      // Running in non-interactive mode
      val pythonPathGenerator = new pyspark.Loader(None).load
        .map(new PythonPathGenerator(_))
        .getOrElse(throw new RuntimeException("Could not find PySpark!"))

      val tempPath = params.tempPath.getOrElse("/tmp/seahorse/download")

      WorkflowExecutor.runInNoninteractiveMode(
        params.copy(tempPath = Some(tempPath)), pythonPathGenerator)
    }
    System.exit(0)
  }

  def configureLogging(): Unit = {
    Option(System.getProperty("logFile"))
      .getOrElse(System.setProperty("logFile", "workflowexecutor"))
    DOMConfigurator.configure(getClass.getResource("/log4j.xml"))
  }


}
