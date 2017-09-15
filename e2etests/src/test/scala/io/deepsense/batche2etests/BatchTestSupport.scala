package io.deepsense.batche2etests

import io.deepsense.commons.models.ClusterDetails
import io.deepsense.e2etests.SeahorseIntegrationTestDSL
import io.deepsense.models.json.workflow.WorkflowWithResultsJsonProtocol
import io.deepsense.models.workflows.WorkflowWithResults
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters.ClusterType

import scala.io.Source
import scalaz.{Failure, Success}
import spray.json._

trait BatchTestSupport extends SeahorseIntegrationTestDSL
  with WorkflowWithResultsJsonProtocol{

  val mesosSparkExecutorConf =
    "spark.executor.uri=http://d3kbcqa49mib13.cloudfront.net/spark-2.0.0-bin-hadoop2.7.tgz"

  def prepareSubmitCommand(sparkSubmitPath: String,
                           envSettings: Map[String, String],
                           masterString: String,
                           specialFlags: Seq[String],
                           workflowPath: String,
                           weJarPath: String,
                           outputDirectory: String): String = {
    val exportsCommandFlat = envSettings.map{
      case(k, v) => s"export $k=$v"
    }.toSeq.mkString(" && ")

    val submitCommandFlat = (
      Seq(
        sparkSubmitPath,
        "--driver-class-path", weJarPath,
        "--class", "io.deepsense.workflowexecutor.WorkflowExecutorApp",
        "--master", masterString,
        "--files", workflowPath
      ) ++
        specialFlags ++
        Seq(
          weJarPath,
          "--workflow-filename", workflowPath,
          "--output-directory", outputDirectory,
          "--custom-code-executors-path", weJarPath
        )
      ).mkString(" ")
    exportsCommandFlat + " && " + submitCommandFlat
  }

  def assertSuccessfulExecution(resultFilePath: String): Unit = {
    val fileContents = Source.fromFile(resultFilePath).mkString
    val workflow = fileContents.parseJson.convertTo[WorkflowWithResults]
    val workflowId = workflow.id
    val workflowName = workflow.workflowInfo.name
    val nodesStatuses = workflow.executionReport.nodesStatuses
    val failedNodes = nodesStatuses.count({ case (k, v) => v.isFailed })
    val completedNodes = nodesStatuses.count({ case (k, v) => v.isCompleted })
    val totalnodes = workflow.graph.nodes.size

    checkCompletedNodesNumber(
      failedNodes,
      completedNodes,
      totalnodes,
      workflowId,
      workflowName
    ) match {
      case Success(_) =>
      case Failure(nodeReport) =>
        fail(s"Some nodes failed for workflow id: $workflowId. name: $workflowName. Node report: $nodeReport")
    }
  }

  // assuming SPARK_HOME is set
  def getEnvSettings(cluster: ClusterDetails): Map[String, String] = {
    val commonSettings = Map(
      "PYTHONPATH" -> "$SPARK_HOME/python:$SPARK_HOME/python/lib/py4j-0.9-src.zip:$PYTHONPATH"
    )
    cluster.clusterType match {
      case ClusterType.local => commonSettings
      case ClusterType.standalone => commonSettings
      case ClusterType.mesos => commonSettings +
        ("LIBPROCESS_ADVERTISE_IP" -> cluster.userIP, "LIBPROCESS_IP" -> cluster.userIP)
      case ClusterType.yarn => commonSettings + ("HADOOP_CONF_DIR" -> cluster.uri)
    }
  }

  def getSpecialFlags(cluster: ClusterDetails): Seq[String] = {
    cluster.clusterType match {
      case ClusterType.local => Seq()
      case ClusterType.standalone => Seq()
      case ClusterType.mesos =>
        Seq("--deploy-mode", "client",
          "--supervise",
          "--conf",
          mesosSparkExecutorConf
        )
      case ClusterType.yarn => Seq("--deploy-mode", "client")
    }
  }

  def getMasterUri(cluster: ClusterDetails): String = {
    cluster.clusterType match {
      case ClusterType.local => "local[*]"
      case ClusterType.yarn => "yarn"
      case _ => cluster.uri
    }
  }

}
