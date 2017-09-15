/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters

import io.deepsense.commons.buildinfo.BuildInfo
import io.deepsense.commons.models.ClusterDetails
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.SparkLauncherConfig
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters.SeahorseSparkLauncher.RichSparkLauncher
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.executor.CommonEnv
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkArgumentParser.SparkOptionsMultiMap
import org.apache.spark.launcher.SparkLauncher

private [clusters] object MesosSparkLauncher {
  import scala.collection.JavaConversions._

  val sparkVersion = BuildInfo.sparkVersion
  val hadoopVersion = BuildInfo.hadoopVersion

  def apply(applicationArgs: Seq[String],
            config: SparkLauncherConfig,
            clusterConfig: ClusterDetails,
            args: SparkOptionsMultiMap): SparkLauncher = {
    new SparkLauncher(env(config, clusterConfig))
      .setSparkArgs(args)
      .setVerbose(true)
      .setMainClass(config.className)
      .setMaster(clusterConfig.uri)
      .setDeployMode("client")
      .setAppResource(config.weJarPath)
      .setSparkHome(config.sparkHome)
      .setAppName("Seahorse Workflow Executor")
      .addAppArgs(applicationArgs: _*)
      .addFile(config.weDepsPath)
      .setConf("spark.executor.uri",
        s"http://d3kbcqa49mib13.cloudfront.net/spark-$sparkVersion-bin-hadoop$hadoopVersion.tgz")
  }

  private def env(
      config: SparkLauncherConfig,
      clusterConfig: ClusterDetails) = CommonEnv(config, clusterConfig) ++ Map(
    "MESOS_NATIVE_JAVA_LIBRARY" -> "/usr/lib/libmesos.so",
    "LIBPROCESS_IP" -> clusterConfig.userIP,
    "LIBPROCESS_ADVERTISE_IP" -> clusterConfig.userIP
  )

}
