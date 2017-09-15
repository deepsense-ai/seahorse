/**
 * Copyright 2016, deepsense.io
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

package io.deepsense.deeplang

import java.net.{Inet4Address, NetworkInterface}
import java.util.UUID

import scala.sys.process.Process

import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.concurrent.Eventually._
import org.scalatest.time.SpanSugar._

import io.deepsense.commons.spark.sql.UserDefinedFunctions
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.doperables.dataframe.DataFrameBuilder
import io.deepsense.deeplang.doperations.readwritedataframe.FileScheme
import io.deepsense.deeplang.inference.InferContext

object StandaloneSparkClusterForTests {

  private var hdfsAddress: String = _
  private var sparkMasterAddress: String = _
  private val runId = UUID.randomUUID().toString.substring(0, 8)
  private val network = s"sbt-test-$runId"

  // This env is used within docker-compose.yml and allows multiple instances of the cluster
  // to run simultaneously
  private val clusterIdEnv = "CLUSTER_ID" -> runId
  private val dockerComposeFile = "docker/spark-standalone-cluster.dc.yml"
  private val dockerComposeCmd = Seq("docker-compose", "-f", dockerComposeFile)

  private val anIpAddress: String = {
    import collection.JavaConverters._

    NetworkInterface.getNetworkInterfaces.asScala.flatMap {
      _.getInetAddresses.asScala.map {
        case ip4: Inet4Address => Some(ip4.getHostAddress)
        case _ => None
      }.filter(_.isDefined)
    }.next().get
  }

  private def dockerInspect(container: String, format: String) =
    Process(Seq("docker", "inspect", "--format", format, s"$container-$runId")).!!.stripLineEnd

  private def address(container: String, port: Int) = {
    val localPort = "{{(index (index .NetworkSettings.Ports \"" + port + "/tcp\") 0).HostPort}}"
    dockerInspect(container, s"$anIpAddress:$localPort")
  }
  def startDockerizedCluster(): Unit = {
    Process("docker/spark-standalone-cluster/build-cluster-node-docker.sh").!
    Process(Seq("docker", "network", "create", "--subnet=10.255.2.1/24", network)).!
    Process(dockerComposeCmd :+ "up" :+ "-d", None, clusterIdEnv).!

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

  def stopDockerizedCluster(): Unit = {
    Process(dockerComposeCmd :+ "kill", None, clusterIdEnv).!
    Process(dockerComposeCmd :+ "down", None, clusterIdEnv).!
    Process(Seq("docker", "network", "rm", network)).!
  }

  def generateSomeHdfsTmpPath(): String =
    FileScheme.HDFS.pathPrefix + s"$hdfsAddress/root/tmp/seahorse_tests/" + UUID.randomUUID()

  lazy val executionContext: ExecutionContext = {
    import org.scalatest.mockito.MockitoSugar._

    System.setProperty("HADOOP_USER_NAME", "root")

    val sparkConf: SparkConf = new SparkConf()
      .setMaster(s"spark://$sparkMasterAddress")
      .setAppName("TestApp")
      .setJars(Seq(
        "./deeplang/target/scala-2.11/" +
          "deepsense-seahorse-deeplang-assembly-1.3.0-LOCAL-SNAPSHOT.jar"
      ))
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .registerKryoClasses(Array())
      .set("spark.executor.cores", "1")
      .set("spark.cores.max", "2")
      .set("spark.executor.memory", "512m")

    val sparkContext = new SparkContext(sparkConf)
    val sparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

    UserDefinedFunctions.registerFunctions(sparkSession.udf)

    val dOperableCatalog = {
      val catalog = new DOperableCatalog
      CatalogRecorder.registerDOperables(catalog)
      catalog
    }

    val inferContext = InferContext(
      DataFrameBuilder(sparkSession),
      "testTenantId",
      dOperableCatalog,
      mock[InnerWorkflowParser])

    new MockedExecutionContext(
      sparkContext,
      sparkSession,
      inferContext,
      LocalFileSystemClient(),
      "testTenantId",
      mock[InnerWorkflowExecutor],
      mock[ContextualDataFrameStorage],
      new MockedContextualCodeExecutor)
  }

}
