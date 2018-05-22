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

package ai.deepsense.deeplang

import java.net.{Inet4Address, NetworkInterface}
import java.util.UUID

import scala.sys.process.Process
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.concurrent.Eventually._
import org.scalatest.time.SpanSugar._

import ai.deepsense.commons.BuildInfo
import ai.deepsense.commons.spark.sql.UserDefinedFunctions
import ai.deepsense.deeplang.doperables.dataframe.DataFrameBuilder
import ai.deepsense.deeplang.doperations.readwritedataframe.FileScheme
import ai.deepsense.sparkutils.SparkSQLSession

object StandaloneSparkClusterForTests {

  private var hdfsAddress: String = _
  private var sparkMasterAddress: String = _
  private val runId = UUID.randomUUID().toString.substring(0, 8)

  // This env is used within docker-compose.yml and allows multiple instances of the cluster
  // to run simultaneously
  private val clusterIdEnv = "CLUSTER_ID" -> runId
  private val clusterManagementScript = "docker/spark-standalone-cluster-manage.sh"

  private val anIpAddress: String = {
    import collection.JavaConverters._

    NetworkInterface.getNetworkInterfaces.asScala.flatMap {
      _.getInetAddresses.asScala.map {
        case ip4: Inet4Address => Some(ip4.getHostAddress)
        case _ => None
      }.filter(_.isDefined)
    }.next().get
  }

  private def address(container: String, port: Int) = {
    def dockerInspect(container: String, format: String) =
      Process(Seq("docker", "inspect", "--format", format, s"$container-$runId")).!!.stripLineEnd

    val localPortFmt = "{{(index (index .NetworkSettings.Ports \"" + port + "/tcp\") 0).HostPort}}"
    s"$anIpAddress:${dockerInspect(container, localPortFmt)}"
  }

  def startDockerizedCluster(): Unit = {
    Process(Seq(clusterManagementScript, "up", sparkVersion), None, clusterIdEnv).!!

    // We turn off the safe mode for hdfs - we don't need it for testing.
    // The loop is here because we don't have control over when the docker is ready
    // for this command.
    eventually(timeout(60.seconds), interval(1.second)) {
      val exec = Seq("docker", "exec", "-u", "root")
      val cmd = Seq("/usr/local/hadoop/bin/hdfs", "dfsadmin", "-safemode", "leave")
      Process(exec ++ Seq(s"hdfs-$runId") ++ cmd, None, clusterIdEnv).!!
    }

    hdfsAddress = address("hdfs", 9000)
    sparkMasterAddress = address("sparkMaster", 7077)
  }

  def stopDockerizedCluster(): Unit =
    Process(Seq(clusterManagementScript, "down", sparkVersion), None, clusterIdEnv).!!

  def generateSomeHdfsTmpPath(): String =
    FileScheme.HDFS.pathPrefix + s"$hdfsAddress/root/tmp/seahorse_tests/" + UUID.randomUUID()

  lazy val executionContext: ExecutionContext = {
    import org.scalatest.mockito.MockitoSugar._

    System.setProperty("HADOOP_USER_NAME", "root")

    val sparkConf: SparkConf = new SparkConf()
      .setMaster(s"spark://$sparkMasterAddress")
      .setAppName("TestApp")
      .setJars(Seq(
        s"./deeplang/target/scala-$majorScalaVersion/seahorse-executor-deeplang-assembly-${BuildInfo.version}.jar"
      ))
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .registerKryoClasses(Array())
      .set("spark.executor.cores", "1")
      .set("spark.cores.max", "2")
      .set("spark.executor.memory", "512m")

    val sparkContext = new SparkContext(sparkConf)
    // After Spark writes all parquet file parts, it reads footers of all of them, merges and writes a "summary".
    // Since this is performed from the driver, it needs to be able to connect to the data node.
    // In our (dockerized) setup it isn't and this is why we turn this option off. It's off in Spark 2.0 by default.
    sparkContext.hadoopConfiguration.set("parquet.enable.summary-metadata", "false")
    val sparkSQLSession = new SparkSQLSession(sparkContext)

    val inferContext = MockedInferContext(
      dataFrameBuilder = DataFrameBuilder(sparkSQLSession)
    )

    ExecutionContext(
      sparkContext,
      sparkSQLSession,
      inferContext,
      ExecutionMode.Batch,
      LocalFileSystemClient(),
      "/tmp",
      "/tmp/library",
      mock[InnerWorkflowExecutor],
      mock[ContextualDataFrameStorage],
      None,
      None,
      new MockedContextualCodeExecutor
    )
  }

  private lazy val majorScalaVersion: String = util.Properties.versionNumberString.split('.').dropRight(1).mkString(".")

  private lazy val sparkVersion: String = org.apache.spark.SPARK_VERSION
}
