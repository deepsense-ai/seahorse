/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters

import org.apache.spark.launcher.SparkLauncher
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters.SeahorseSparkLauncher.RichSparkLauncher
import io.deepsense.sessionmanager.rest.requests.ClusterDetails
import io.deepsense.sessionmanager.service.sessionspawner.SessionConfig
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.SparkLauncherConfig
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.executor.{CommonEnv, SessionExecutorArgs}
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkAgumentParser.UnknownOption

import scalaz.Validation

private [clusters] object StandaloneSparkLauncher {
  import scala.collection.JavaConversions._

  def apply(
      sessionConfig: SessionConfig,
      config: SparkLauncherConfig,
      clusterConfig: ClusterDetails): Validation[UnknownOption, SparkLauncher] = for {
    args <- clusterConfig.parsedParams
  } yield new SparkLauncher(CommonEnv(config, clusterConfig))
      .setSparkArgs(args.toMap)
      .setVerbose(true)
      .setMainClass(config.className)
      .setMaster(clusterConfig.uri)
      .setDeployMode("client")
      .setAppResource(config.weJarPath)
      .setAppName("SessionExecutor")
      .addAppArgs(SessionExecutorArgs(sessionConfig, config, clusterConfig): _*)
      .addFile(config.weDepsPath)
      .setConf("spark.driver.host", clusterConfig.userIP)
      .setConf("spark.executorEnv.PYTHONPATH", config.weDepsPath)
      .setConf("spark.driver.extraClassPath", config.weJarPath)
      .setConf("spark.driver.extraJavaOptions",
        "-XX:MaxPermSize=1024m -XX:PermSize=256m -Dfile.encoding=UTF8")
}
