/**
  * Copyright (c) 2016, CodiLime Inc.
  */
package io.deepsense.batche2etests

import java.io.File

import scala.sys.process._

import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import io.deepsense.batche2etests.JsonWorkflowsBatchTest.{ProcExitError, ProcExitSuccessful}
import io.deepsense.commons.models.ClusterDetails
import io.deepsense.e2etests.{TestClusters, TestWorkflowsIterator}

class JsonWorkflowsBatchTest
  extends WordSpec
  with Matchers
  with BatchTestSupport
  with BeforeAndAfterAll {

  val resultFilePath = "result.json"
  val sparkVersion = org.apache.spark.SPARK_VERSION
  val hadoopVersion = "2.7"
  override val mesosSparkExecutorConf =
    s"spark.mesos.executor.home=/spark-$sparkVersion/spark-$sparkVersion-bin-hadoop$hadoopVersion/"
  val dockerComposePath = "../deployment/docker-compose/"

  TestWorkflowsIterator.foreach { case TestWorkflowsIterator.Input(modifiedFilename, _) =>
    val path = getOriginalPath(modifiedFilename)
    s"Workflow loaded from '$path'" should {
      "should complete successfully in batch mode" when {
        for (cluster <- TestClusters.allAvailableClusters) {
          s"run on ${cluster.clusterType} cluster" in {
            clearExecutionResult(resultFilePath)
            runWorkflow(cluster, path)
            try {
              assertSuccessfulExecution(resultFilePath)
            } finally {
              clearExecutionResult(resultFilePath)
            }
          }
        }
      }
    }
  }

  val sessionmanagerDockerId = getContainerId("sessionmanager", s"$dockerComposePath/docker-compose.yml")

  override def afterAll(): Unit = {
    reportProcessExecution(executeProcessGatherOutput(bashExecutionOnDockerCommand(
      sessionmanagerDockerId,
      "cp workflowexecutor_seahorse*.log /spark_applications_logs"
    )))
  }

  private def runWorkflow(cluster: ClusterDetails,
                          path: String): Unit = {

    val envSettings = getEnvSettings(cluster)
    val specialFlags = getSpecialFlags(cluster)
    val masterString = getMasterUri(cluster)

    val dockerBaseDir = "/opt/docker/"

    val workflowPath = dockerBaseDir + "test-workflow.json"
    val weJarPath = dockerBaseDir + "we.jar"
    val outputDirectory = dockerBaseDir + "test-output/"
    val dockerResultFilePath = outputDirectory + "result.json"
    val sparkSubmitPath = "$SPARK_HOME/bin/spark-submit"


    val localWorkflowsDirectory = "src/test/resources/workflows/"
    val localResultFilePath = resultFilePath

    val workflowToDockerCommand = Seq(
      "docker",
      "cp",
      localWorkflowsDirectory + path,
      s"$sessionmanagerDockerId:$workflowPath"
    )

    val workflowFromDockerCommand = Seq(
      "docker",
      "cp",
      s"$sessionmanagerDockerId:$dockerResultFilePath",
      localResultFilePath
    )

    val submitCommand = prepareSubmitCommand(
      sparkSubmitPath,
      envSettings,
      masterString,
      specialFlags,
      workflowPath,
      weJarPath,
      outputDirectory
    )

    val workflowExecutionCommand =
      bashExecutionOnDockerCommand(sessionmanagerDockerId, submitCommand)

    val runLogs: Either[ProcExitError, ProcExitSuccessful] = for {
      p1 <- executeProcessGatherOutput(workflowToDockerCommand).right
      p2 <- executeProcessGatherOutput(workflowExecutionCommand).right
      p3 <- executeProcessGatherOutput(workflowFromDockerCommand).right
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

  private def clearExecutionResult(resultFilePath: String): Unit = {
    val file = new File(resultFilePath)
    if (file.exists) {
      file.delete()
    }
  }

  private def getContainerId(serviceName: String, dockerComposeFilePath: String): String = {
    Seq("docker-compose", "-f", dockerComposeFilePath, "ps", "-q", serviceName).!!.trim
  }

  private def getOriginalPath(path: String) = path.replace("%20", " ")
}

object JsonWorkflowsBatchTest {

  case class ProcExitSuccessful(stdOut: String, stdErr: String) {
    def +(other: ProcExitSuccessful): ProcExitSuccessful = {
      ProcExitSuccessful(this.stdOut + other.stdOut, this.stdErr + other.stdErr)
    }
  }

  case class ProcExitError(exitCode: Int, command: String, stdOut: String, stdErr: String)

}
