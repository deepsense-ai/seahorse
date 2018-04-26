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

package ai.deepsense.workflowexecutor

import java.net.URL

import org.apache.log4j.xml.DOMConfigurator
import scopt.OptionParser

import ai.deepsense.commons.utils.Logging
import ai.deepsense.workflowexecutor.buildinfo.BuildInfo
import ai.deepsense.workflowexecutor.executor.{SessionExecutor, WorkflowExecutor}
import ai.deepsense.workflowexecutor.pyspark.PythonPathGenerator

/**
 * WorkflowExecutor
 * workflow file name has to be passed via command-line parameter
 */
object WorkflowExecutorApp extends Logging {

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

    opt[String]("wm-username") hidden() valueName "USER" action {
      (x, c) => c.copy(wmUsername = Some(x))
    } text "user for accessing Workflow Manager API"

    opt[String]("wm-password") hidden() valueName "PASSWORD" action {
      (x, c) => c.copy(wmPassword = Some(x))
    } text "password for accessing Workflow Manager API"

    opt[String]("mail-server-host") hidden() valueName "HOST" action {
      (x, c) => c.copy(mailParams = c.mailParams.copy(mailServerHost = Some(x)))
    }

    opt[Int]("mail-server-port") hidden() valueName "PORT" action {
      (x, c) => c.copy(mailParams = c.mailParams.copy(mailServerPort = Some(x)))
    }

    opt[String]("mail-server-user") hidden() valueName "USER" action {
      (x, c) => c.copy(mailParams = c.mailParams.copy(mailServerUser = Some(x)))
    }

    opt[String]("mail-server-password") hidden() valueName "PASS" action {
      (x, c) => c.copy(mailParams = c.mailParams.copy(mailServerPassword = Some(x)))
    }

    opt[String]("mail-server-sender") hidden() valueName "EMAIL" action {
      (x, c) => c.copy(mailParams = c.mailParams.copy(mailServerSender = Some(x)))
    }

    opt[String]("notebook-server-address") hidden() valueName "URL" action {
      (x, c) => c.copy(notebookServerAddress = Some(new URL(x)))
    }

    opt[String]("datasource-server-address") hidden() valueName "URL" action {
      (x, c) => c.copy(datasourceServerAddress = Some(new URL(x)))
    }

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

    note("Visit https://seahorse.deepsense.ai for more details")

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
          "--custom-code-executors-path is forbidden in interactive mode"),
        (config.mailParams.mailServerHost.isEmpty,
          "--mail-server-host is required in interactive mode"),
        (config.mailParams.mailServerPort.isEmpty,
          "--mail-server-port is required in interactive mode"),
        (config.mailParams.mailServerUser.isEmpty,
          "--mail-server-user is required in interactive mode"),
        (config.mailParams.mailServerPassword.isEmpty,
          "--mail-server-password is required in interactive mode"),
        (config.mailParams.mailServerSender.isEmpty,
          "--mail-server-sender is required in interactive mode"),
        (config.notebookServerAddress.isEmpty,
          "--notebook-server-address is required in interactive mode"),
        (config.datasourceServerAddress.isEmpty,
          "--datasource-server-address is required in interactive mode")
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

  def main(args: Array[String]): Unit = {
    configureLogging()

    val paramsOpt = parser.parse(args, ExecutionParams())
    val params = paramsOpt.getOrElse(sys.exit(1))

    if (params.interactiveMode) {
      // Interactive mode (SessionExecutor)
      SessionExecutor(
        messageQueueHost = params.messageQueueHost.get,
        messageQueuePort = params.messageQueuePort.get,
        messageQueueUser = params.messageQueueUser.get,
        messageQueuePass = params.messageQueuePass.get,
        workflowId = params.workflowId.get,
        wmAddress = params.wmAddress.get,
        wmUsername = params.wmUsername.get,
        wmPassword = params.wmPassword.get,
        mailServerHost = params.mailParams.mailServerHost.get,
        mailServerPort = params.mailParams.mailServerPort.get,
        mailServerUser = params.mailParams.mailServerUser.get,
        mailServerPassword = params.mailParams.mailServerPassword.get,
        mailServerSender = params.mailParams.mailServerSender.get,
        notebookServerAddress = params.notebookServerAddress.get,
        datasourceServerAddress = params.datasourceServerAddress.get,
        depsZip = params.depsZip.get,
        workflowOwnerId = params.userId.get,
        tempPath = params.tempPath.get,
        pythonBinaryPath = params.pythonBinaryPath
      ).execute()
    } else {
      // Running in non-interactive mode
      val pythonPathGenerator = pyspark.Loader.load
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
