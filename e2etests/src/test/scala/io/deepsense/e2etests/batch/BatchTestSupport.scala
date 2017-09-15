/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.e2etests.batch

import java.io.File
import java.net.URL

import io.deepsense.commons.buildinfo.BuildInfo
import spray.json._

import scala.io.Source
import scalaz.{Failure, Success}
import io.deepsense.commons.models.ClusterDetails
import io.deepsense.e2etests.{SeahorseIntegrationTestDSL, TestDatasourcesInserter}
import io.deepsense.models.json.workflow.WorkflowWithResultsJsonProtocol
import io.deepsense.models.workflows.WorkflowWithResults
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters.ClusterType

import scala.io.Source
import scalaz.{Failure, Success}
import spray.json._

trait BatchTestSupport
  extends SeahorseIntegrationTestDSL
  with TestDatasourcesInserter
  with WorkflowWithResultsJsonProtocol {

  val sparkVersion = BuildInfo.sparkVersion
  val hadoopVersion = BuildInfo.hadoopVersion

  val mesosSparkExecutorConf =
    s"spark.executor.uri=http://d3kbcqa49mib13.cloudfront.net/spark-$sparkVersion-bin-hadoop$hadoopVersion.tgz"

  def prepareSubmitCommand(
      sparkSubmitPath: String,
      cluster: ClusterDetails,
      workflowPath: File,
      weJarPath: File,
      additionalJars: Seq[URL],
      outputDirectory: File): String = {

    val envSettings = getEnvSettings(cluster)
    val specialFlags = getSpecialFlags(cluster)
    val masterString = getMasterUri(cluster)

    val exportsCommandFlat = envSettings.map{
      case(k, v) => s"export $k=$v"
    }.toSeq.mkString(" && ")

    val submitCommandFlat = (
      Seq(
        sparkSubmitPath,
        "--driver-class-path", weJarPath,
        "--class", "io.deepsense.workflowexecutor.WorkflowExecutorApp",
        "--master", masterString,
        "--files", workflowPath,
        if (additionalJars.nonEmpty) {
          "--jars " + additionalJars.map(_.toString).mkString("\"", ",", "\"")
        } else {
          /*
             We cannot pass a --jars <empty string> option because it would later get translated into a real path,
             equal to whichever the current working directory is (most probably /opt/docker), and that in turn would be
             considered by spark to be a (non-existing) jar name. This will ultimately result in a strange error
             mentioning something about a stream that cannot be open (we had stumbled upon a stream named /jars/docker
             - the /jars part is hardcoded somewhere in the entrails of spark/netty, and the docker part was the name
             of our "jar"), hence - no --jars option at all.
           */
          ""
        }
      ).filterNot(_.toString.isEmpty) ++
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

  def assertSuccessfulExecution(resultFile: File): Unit = {
    val fileContents = Source.fromFile(resultFile).mkString
    val workflow = fileContents.parseJson.convertTo[WorkflowWithResults]
    val workflowId = workflow.id
    val workflowName = workflow.workflowInfo.name
    val nodesStatuses = workflow.executionReport.nodesStatuses
    val failedNodes = nodesStatuses.count({ case (k, v) => v.isFailed })
    val completedNodes = nodesStatuses.count({ case (k, v) => v.isCompleted })
    val totalNodes = workflow.graph.nodes.size

    checkCompletedNodesNumber(
      failedNodes,
      completedNodes,
      totalNodes,
      workflowId,
      workflowName
    ) match {
      case Success(_) =>
      case Failure(nodeReport) =>
        fail(s"Some nodes failed for workflow id: $workflowId. name: $workflowName. Node report: $nodeReport")
    }
  }

  // assuming SPARK_HOME is set
  private def getEnvSettings(cluster: ClusterDetails): Map[String, String] = {
    val commonSettings = Map(
      "PYTHONPATH" -> "$SPARK_HOME/python:$PYTHONPATH"
    )
    cluster.clusterType match {
      case ClusterType.local => commonSettings
      case ClusterType.standalone => commonSettings
      case ClusterType.mesos => commonSettings +
        ("LIBPROCESS_ADVERTISE_IP" -> cluster.userIP, "LIBPROCESS_IP" -> cluster.userIP)
      case ClusterType.yarn => commonSettings + ("HADOOP_CONF_DIR" -> cluster.uri)
    }
  }

  private def getSpecialFlags(cluster: ClusterDetails): Seq[String] = {
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

  private def getMasterUri(cluster: ClusterDetails): String = {
    cluster.clusterType match {
      case ClusterType.local => "local[*]"
      case ClusterType.yarn => "yarn"
      case _ => cluster.uri
    }
  }

}
