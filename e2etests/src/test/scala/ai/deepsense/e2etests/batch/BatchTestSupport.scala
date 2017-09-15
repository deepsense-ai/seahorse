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

import java.io.File
import java.net.URL

import ai.deepsense.commons.buildinfo.BuildInfo
import spray.json._

import scala.io.Source
import scalaz.{Failure, Success}
import ai.deepsense.commons.models.ClusterDetails
import ai.deepsense.e2etests.{SeahorseIntegrationTestDSL, TestDatasourcesInserter}
import ai.deepsense.models.json.workflow.WorkflowWithResultsJsonProtocol
import ai.deepsense.models.workflows.WorkflowWithResults
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters.ClusterType

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
    val specialFlags = getSpecialFlags(cluster, additionalJars)
    val masterString = getMasterUri(cluster)
    val additionalClusterOptions: Seq[String] = cluster.executorMemory.toSeq.flatMap {
      memory => Seq("--executor-memory", memory)
    }

    val exportsCommandFlat = envSettings.map{
      case(k, v) => s"export $k=$v"
    }.toSeq.mkString(" && ")

    val files = {
      Seq(workflowPath.toString) ++ {
        if (cluster.clusterType == ClusterType.yarn) {
          // In YARN --jars only will only affect classpath - it won't cause jars to be uploaded.
          // To actually deliver jar file onto cluster you need to additionally include it with --files
          additionalJars.map(_.toString)
        } else {
          Seq.empty
        }
      }
    }.asCsvForShellCommand

    val submitCommandFlat = (
      Seq(
        sparkSubmitPath,
        "--driver-class-path", weJarPath,
        "--class", "ai.deepsense.workflowexecutor.WorkflowExecutorApp",
        "--master", masterString,
        "--files", files
      )
      ++ additionalClusterOptions
      ++ Seq(
        if (additionalJars.nonEmpty) {
          "--jars " + additionalJars.asCsvForShellCommand
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
      ).filterNot(_.toString.isEmpty)
      ++ specialFlags
      ++ Seq(
          weJarPath,
          "--workflow-filename", workflowPath,
          "--output-directory", outputDirectory,
          "--custom-code-executors-path", weJarPath
        )
      ).mkString(" ")

    if (exportsCommandFlat == "") {
      submitCommandFlat
    } else {
      exportsCommandFlat + " && " + submitCommandFlat
    }
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
  private def getEnvSettings(cluster: ClusterDetails): Map[String, String] = cluster.clusterType match {
    case ClusterType.local => Map()
    case ClusterType.standalone => Map()
    case ClusterType.mesos => Map("LIBPROCESS_ADVERTISE_IP" -> cluster.userIP, "LIBPROCESS_IP" -> cluster.userIP)
    case ClusterType.yarn => Map("HADOOP_CONF_DIR" -> cluster.uri, "HADOOP_USER_NAME" -> "hdfs")
  }

  private def getSpecialFlags(cluster: ClusterDetails, additionalJars: Seq[URL]): Seq[String] = {
    cluster.clusterType match {
      case ClusterType.local => Seq()
      case ClusterType.standalone => Seq()
      case ClusterType.mesos => Seq(
        "--deploy-mode", "client",
        "--supervise",
        "--conf",
        mesosSparkExecutorConf
      )
      case ClusterType.yarn => Seq(
        "--deploy-mode", "client",
        "--conf", "spark.yarn.dist.archives=$SPARK_HOME/R/lib/sparkr.zip#sparkr"
      )
    }
  }

  private def getMasterUri(cluster: ClusterDetails): String = {
    cluster.clusterType match {
      case ClusterType.local => "local[*]"
      case ClusterType.yarn => "yarn"
      case _ => cluster.uri
    }
  }

  private implicit class SeqOpts[T](val seq: Seq[T]) {
    def asCsvForShellCommand = seq.mkString("\"", ",", "\"")
  }

}
