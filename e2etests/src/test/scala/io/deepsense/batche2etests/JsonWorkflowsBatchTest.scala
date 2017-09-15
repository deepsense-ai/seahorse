/**
  * Copyright (c) 2016, CodiLime Inc.
  */
package io.deepsense.batche2etests

import java.io.File

import io.deepsense.commons.models.ClusterDetails
import io.deepsense.e2etests.{TestClusters, TestWorkflowsIterator}
import org.scalatest.{Matchers, WordSpec}

import scala.sys.process._

class JsonWorkflowsBatchTest extends WordSpec
  with Matchers
  with BatchTestSupport {

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
            assertSuccessfulExecution(resultFilePath)
          }
          clearExecutionResult(resultFilePath)
        }
      }
    }
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

    val sessionmanagerDockerId = getContainerId("sessionmanager", s"$dockerComposePath/docker-compose.yml")
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

    workflowToDockerCommand.!!
    workflowExecutionCommand.!!
    workflowFromDockerCommand.!!
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
