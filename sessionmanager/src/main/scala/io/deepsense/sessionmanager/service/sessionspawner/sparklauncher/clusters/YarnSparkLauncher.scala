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
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkAgumentParser
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkAgumentParser.UnknownOption

private [clusters] object YarnSparkLauncher {
  import scala.collection.JavaConversions._

  def apply(
      sessionConfig: SessionConfig,
      config: SparkLauncherConfig,
      clusterConfig: ClusterDetails): Validation[UnknownOption, SparkLauncher] = for {
    args <- SparkAgumentParser.parseAsMap(clusterConfig.params)
  } yield new SparkLauncher(env(config, clusterConfig))
      .setSparkArgs(args.toMap)
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

  private def env(
      config: SparkLauncherConfig,
      clusterConfig: ClusterDetails) = CommonEnv(config, clusterConfig) ++ Map(
    "HADOOP_CONF_DIR" -> clusterConfig.uri,
    "SPARK_YARN_MODE" -> "true"
  )

}
