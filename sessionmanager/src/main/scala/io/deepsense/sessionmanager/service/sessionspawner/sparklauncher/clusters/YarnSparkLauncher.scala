/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters

import org.apache.spark.launcher.SparkLauncher

import io.deepsense.sessionmanager.rest.requests.ClusterDetails
import io.deepsense.sessionmanager.service.sessionspawner.SessionConfig
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.SparkLauncherConfig
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.executor.SessionExecutorArgs

private [clusters] object YarnSparkLauncher {
  import scala.collection.JavaConversions._

  def apply(
      sessionConfig: SessionConfig,
      config: SparkLauncherConfig,
      clusterConfig: ClusterDetails): SparkLauncher = {
    new SparkLauncher(env(clusterConfig))
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
      .setConf("spark.yarn.appMasterEnv.PYSPARK_PYTHON", config.pythonBinary)
  }

  private def env(clusterConfig: ClusterDetails): Map[String, String] = Map(
    "HADOOP_CONF_DIR" -> clusterConfig.uri,
    "SPARK_YARN_MODE" -> "true",
    "HADOOP_USER_NAME" -> clusterConfig.hadoopUser.getOrElse("root"),
    "SPARK_LOCAL_IP" -> clusterConfig.userIP
  )

}
