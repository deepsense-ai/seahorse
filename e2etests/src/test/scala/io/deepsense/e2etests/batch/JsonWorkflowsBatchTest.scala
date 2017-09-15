/**
  * Copyright (c) 2016, CodiLime Inc.
  */
package io.deepsense.e2etests.batch

import java.io.{File, PrintWriter}

import scala.concurrent.Await
import scala.sys.process._

import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import io.deepsense.e2etests.batch.JsonWorkflowsBatchTest.{ProcExitError, ProcExitSuccessful}
import io.deepsense.commons.models.ClusterDetails
import io.deepsense.commons.utils.FileOpts._
import io.deepsense.commons.utils.OptionOpts._
import io.deepsense.e2etests.{TestClusters, TestWorkflowsIterator, WorkflowParser}
import io.deepsense.models.workflows.WorkflowWithVariables

class JsonWorkflowsBatchTest
  extends WordSpec
  with Matchers
  with BatchTestSupport
  with BeforeAndAfterAll {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val testsDir = new File("target", "test-batch")

  private val resultsDir = new File(testsDir, "results")

  private val dockerComposePath = "../deployment/docker-compose/"

  ensureSeahorseIsRunning()

  insertDatasourcesForTest()

  TestWorkflowsIterator.foreach { case TestWorkflowsIterator.Input(uri, file, fileContents) =>
    s"Workflow loaded from '$uri'" should {
      "should complete successfully in batch mode" when {
        for (cluster <- TestClusters.allAvailableClusters) {
          s"run on ${cluster.clusterType} cluster" in {
            val future = for {
              workflowInfo <- uploadWorkflow(fileContents)
              workflowId = workflowInfo.id
              workflowWithVariablesOpt <- wmclient.downloadWorkflow(workflowId)
              workflowWithVariables <- workflowWithVariablesOpt.asFuture
              _ <- wmclient.deleteWorkflow(workflowId)
            } yield {
              val downloadedFile = new File(testsDir, uri.toString)
              saveWorkflowToFile(downloadedFile, workflowWithVariables)
              logger.info(s"Saved downloaded workflow under $downloadedFile")
              val resultFile = new File(resultsDir, s"${workflowId}_${file.getName}")
              resultFile.createPathToFile()
              runWorkflow(cluster, downloadedFile, resultFile)
              logger.info(s"Saved result under $resultFile")
              copyWorkflowExecutorLogs(s"workflowexecutor_$workflowId.log")
              assertSuccessfulExecution(resultFile)
            }
            Await.result(future, workflowTimeout)
          }
        }
      }
    }

  }

  private val sessionmanagerDockerId = getContainerId("sessionmanager", s"$dockerComposePath/docker-compose.yml")

  private def copyWorkflowExecutorLogs(fileName: String): Unit = {
    reportProcessExecution(executeProcessGatherOutput(bashExecutionOnDockerCommand(
      sessionmanagerDockerId,
      s"cp workflowexecutor_seahorse*.log /spark_applications_logs/$fileName"
    )))
  }

  private def runWorkflow(
      cluster: ClusterDetails,
      inputFile: File,
      resultFile: File): Unit = {

    val envSettings = getEnvSettings(cluster)
    val specialFlags = getSpecialFlags(cluster)
    val masterString = getMasterUri(cluster)

    val dockerBaseDir = new File("/opt/docker/")

    val workflowPath = new File(dockerBaseDir, "test-workflow.json")
    val weJarPath = new File(dockerBaseDir, "we.jar")

    val localJarsDir = new File(dockerComposePath, "jars")
    val localJarPaths = getJarsFrom(localJarsDir)
    val jarsInDockerPaths = localJarPaths.map { case f => new File("/resources/jars", f.getName)}
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
      envSettings,
      masterString,
      specialFlags,
      workflowPath,
      weJarPath,
      jarsInDockerPaths,
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
        logger.error(s"Shell command '$cmd' exited with code: $exitCode")
        logOutAndErr(out, err, logger.error)
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
    val exitCode = cmd ! ProcessLogger(out ++= _, err ++= _)

    if (exitCode == 0) {
      Right(ProcExitSuccessful(out.toString, err.toString))
    } else {
      Left(ProcExitError(exitCode, cmd.mkString(" "), out.toString, err.toString))
    }
  }

  private def bashExecutionOnDockerCommand(dockerName: String, command: String): Seq[String] = {
    Seq("docker", "exec", dockerName, "bash", "-c", command)
  }

  private def getContainerId(serviceName: String, dockerComposeFilePath: String): String = {
    Seq("docker-compose", "-f", dockerComposeFilePath, "ps", "-q", serviceName).!!.trim
  }

  private def saveWorkflowToFile(file: File, workflowWithVariables: WorkflowWithVariables): Unit = {
    file.createPathToFile()
    file.createNewFile()
    val raw = WorkflowParser.printWorkflow(workflowWithVariables, prettyPrint = true)
    new PrintWriter(file) {
      write(raw)
      close()
    }
  }

  private def getJarsFrom(dir: File): Seq[File] = {
    dir.listFiles.filter(f => f.isFile && f.getName.endsWith(".jar"))
  }
}

object JsonWorkflowsBatchTest {

  case class ProcExitSuccessful(stdOut: String, stdErr: String) {
    def +(other: ProcExitSuccessful): ProcExitSuccessful = {
      ProcExitSuccessful(this.stdOut + other.stdOut, this.stdErr + other.stdErr)
    }
  }

  case class ProcExitError(exitCode: Int, command: String, stdOut: String, stdErr: String)

}
