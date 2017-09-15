/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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
package ai.deepsense.e2etests.batch

import java.io.{File, PrintWriter}

import scala.concurrent.Future
import scala.sys.process._

import ai.deepsense.commons.models.ClusterDetails
import ai.deepsense.commons.utils.FileOpts._
import ai.deepsense.commons.utils.OptionOpts._
import ai.deepsense.e2etests.WorkflowJsonConverter
import ai.deepsense.e2etests.batch.BatchTestInDockerSupport._
import ai.deepsense.models.workflows.{Workflow, WorkflowWithVariables}

trait BatchTestInDockerSupport extends BatchTestSupport {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val testsDir = new File("target", "test-batch")

  private val resultsDir = new File(testsDir, "results")

  protected def testWorkflowFromSeahorse(
      cluster: ClusterDetails,
      fileName: String,
      workflowId: Workflow.Id): Future[Unit] = {

    val cleanedFileName = cleanFileName(fileName)

    for {
      workflowWithVariablesOpt <- wmclient.downloadWorkflow(workflowId)
      workflowWithVariables <- workflowWithVariablesOpt.asFuture
      _ <- wmclient.deleteWorkflow(workflowId)
    } yield {
      val downloadedFile = new File(testsDir, cleanedFileName)
      saveWorkflowToFile(downloadedFile, workflowWithVariables)
      logger.info(s"Saved downloaded workflow under $downloadedFile")
      val resultFile = new File(resultsDir, s"${workflowId}_$cleanedFileName")
      resultFile.createPathToFile()
      runWorkflow(cluster, downloadedFile, resultFile)
      logger.info(s"Saved result under $resultFile")
      copyWorkflowExecutorLogs(s"workflowexecutor_$workflowId.log")
      assertSuccessfulExecution(resultFile)
    }
  }

  private def cleanFileName(fileName: String) = fileName.replaceAll("[^a-zA-Z0-9.-]", "_")

  private val sessionmanagerDockerId = getContainerId("sessionmanager")

  private def copyWorkflowExecutorLogs(fileName: String): Unit = {
    val commandToFindLastLogFile = "ls -t workflowexecutor_seahorse*.log | head -1"
    reportProcessExecution(executeProcessGatherOutput(bashExecutionOnDockerCommand(
      sessionmanagerDockerId,
      s"cp `$commandToFindLastLogFile` /spark_applications_logs/$fileName"
    )))
  }

  private def runWorkflow(
      cluster: ClusterDetails,
      inputFile: File,
      resultFile: File): Unit = {

    val dockerBaseDir = new File("/opt/docker/")

    val workflowPath = new File(dockerBaseDir, "test-workflow.json")
    val weJarPath = new File(dockerBaseDir, "we.jar")

    val outputDirectory = new File(dockerBaseDir, "test-output/")
    val dockerResultFilePath = new File(outputDirectory, "result.json")
    val sparkSubmitPath = "$SPARK_HOME/bin/spark-submit"

    val workflowToDockerCommand = Seq(
      "docker",
      "cp",
      inputFile.getPath,
      s"$sessionmanagerDockerId:$workflowPath"
    )

    val resultFromDockerCommand = Seq(
      "docker",
      "cp",
      s"$sessionmanagerDockerId:$dockerResultFilePath",
      resultFile.getPath
    )

    val submitCommand = prepareSubmitCommand(
      sparkSubmitPath,
      cluster,
      workflowPath,
      weJarPath,
      additionalJars = jarsInDockerPaths,
      outputDirectory
    )

    logger.info(s"Submit command: $submitCommand")

    val workflowExecutionCommand =
      bashExecutionOnDockerCommand(sessionmanagerDockerId, submitCommand)

    val runLogs: Either[ProcExitError, ProcExitSuccessful] = for {
      p1 <- executeProcessGatherOutput(workflowToDockerCommand).right
      p2 <- executeProcessGatherOutput(workflowExecutionCommand).right
      p3 <- executeProcessGatherOutput(resultFromDockerCommand).right
    } yield {
      p1 + p2 + p3
    }

    reportProcessExecution(runLogs)
  }

  private def reportProcessExecution(execution: Either[ProcExitError, ProcExitSuccessful]): Unit = {
    execution match {
      case Left(ProcExitError(exitCode, cmd, out, err)) =>
        val errorMessage = s"Shell command '$cmd' exited with code: $exitCode"
        logger.error(errorMessage)
        logOutAndErr(out, err, logger.error)
        fail(errorMessage)
      case Right(ProcExitSuccessful(out, err)) =>
        logOutAndErr(out, err, logger.info)
    }
  }

  private def logOutAndErr(out: String, err: String, loggingFunction: String => Unit): Unit = {
    loggingFunction(s"Standard output: $out")
    loggingFunction(s"Standard error: $err")
  }

  private def executeProcessGatherOutput(cmd: Seq[String]): Either[ProcExitError, ProcExitSuccessful] = {
    val out = new StringBuilder
    val err = new StringBuilder
    def appendLn(sb: StringBuilder)(s: String): Unit = {
      sb ++= s
      sb += '\n'
    }
    val exitCode = cmd ! ProcessLogger(appendLn(out) , appendLn(err))

    if (exitCode == 0) {
      Right(ProcExitSuccessful(out.toString, err.toString))
    } else {
      Left(ProcExitError(exitCode, cmd.mkString(" "), out.toString, err.toString))
    }
  }

  private def bashExecutionOnDockerCommand(dockerName: String, command: String): Seq[String] = {
    Seq("docker", "exec", dockerName, "bash", "-c", command)
  }

  private def getContainerId(serviceName: String): String = {
    val containerIds = Seq("docker", "ps", "-q", "--filter", s"name=$serviceName").!!.split("\\s+")
    logger.info(s"Found containers with '$serviceName' in name: ${containerIds.mkString(", ")}")
    containerIds.toSeq match {
      case Seq(containerId) => containerId
      case Seq() => fail(s"No containers with '$serviceName' in name. Test cannot continue")
      case _ => fail(s"More than one container with '$serviceName' in name. Test cannot continue")
    }
  }

  private def saveWorkflowToFile(file: File, workflowWithVariables: WorkflowWithVariables): Unit = {
    file.createPathToFile()
    file.createNewFile()
    val raw = new WorkflowJsonConverter(graphReader)
      .printWorkflow(workflowWithVariables, prettyPrint = true)
    new PrintWriter(file) {
      write(raw)
      close()
    }
  }
}

object BatchTestInDockerSupport {

  case class ProcExitSuccessful(stdOut: String, stdErr: String) {
    def +(other: ProcExitSuccessful): ProcExitSuccessful = {
      ProcExitSuccessful(this.stdOut + other.stdOut, this.stdErr + other.stdErr)
    }
  }

  case class ProcExitError(exitCode: Int, command: String, stdOut: String, stdErr: String)

}
