/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters

import scalaz.Validation

import org.apache.spark.launcher.SparkLauncher

import io.deepsense.commons.models.ClusterDetails
import io.deepsense.sessionmanager.service.sessionspawner.SessionConfig
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.SparkLauncherConfig
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters.SeahorseSparkLauncher.RichSparkLauncher
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.executor.{CommonEnv, SessionExecutorArgs}
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkArgumentParser.SparkOptionsMultiMap
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkArgumentParser.SparkOptionsOp

private [clusters] object YarnSparkLauncher {
  import scala.collection.JavaConversions._

  def apply(sessionConfig: SessionConfig,
            config: SparkLauncherConfig,
            clusterConfig: ClusterDetails,
            args: SparkOptionsMultiMap): SparkLauncher = {
    val updatedArgs = args.updateConfOptions(
      "spark.yarn.dist.archives", sparkRArchivePath(config.sparkHome))
    new SparkLauncher(env(config, clusterConfig))
      .setSparkArgs(updatedArgs)
      .setVerbose(true)
      .setMainClass(config.className)
      .setMaster("yarn")
      .setDeployMode("client")
      .setAppResource(config.weJarPath)
      .setSparkHome(config.sparkHome)
      .setAppName("SessionExecutor")
      .addAppArgs(SessionExecutorArgs(sessionConfig, config, clusterConfig): _*)
      .addFile(config.weDepsPath)
      .setConf("spark.driver.host", clusterConfig.userIP)
      .setConf("spark.driver.extraClassPath", config.weJarPath)
      .setConf("spark.executorEnv.PYTHONPATH", config.weDepsFileName)
      .setConf("spark.yarn.appMasterEnv.PYSPARK_PYTHON", config.pythonDriverBinary)
  }


  private def sparkRArchivePath(sparkHome: String, linkName: String = "#sparkr") =
    sparkHome + "/R/lib/sparkr.zip" + linkName

  private def env(
      config: SparkLauncherConfig,
      clusterConfig: ClusterDetails) = CommonEnv(config, clusterConfig) ++ Map(
    "HADOOP_CONF_DIR" -> clusterConfig.uri,
    "SPARK_YARN_MODE" -> "true"
  )

}
