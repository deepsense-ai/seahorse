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
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.executor.{CommonEnv, LocalSessionExecutorArgs}
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkArgumentParser._

private [clusters] object LocalSparkLauncher {

  import scala.collection.JavaConversions._

  def apply(sessionConfig: SessionConfig,
            config: SparkLauncherConfig,
            clusterConfig: ClusterDetails,
            args: SparkOptionsMultiMap): SparkLauncher = {
    new SparkLauncher(env(config, clusterConfig))
      .setSparkArgs(args)
      .setVerbose(true)
      .setMainClass(config.className)
      .setMaster("local[*]")
      .setDeployMode("client")
      .setAppResource(config.weJarPath)
      .setAppName("SessionExecutor")
      .addAppArgs(LocalSessionExecutorArgs(sessionConfig, config, clusterConfig): _*)
      .addFile(config.weDepsPath)
      .setConf("spark.executorEnv.PYTHONPATH", config.weDepsPath)
      .setConf("spark.default.parallelism", parallelism.toString)
  }

  private def env(config: SparkLauncherConfig,
                  clusterConfig: ClusterDetails) = CommonEnv(config, clusterConfig) ++ Map(
    // For local cluster driver python binary IS executors python binary
    "PYSPARK_PYTHON" -> config.pythonDriverBinary
  )

  /**
    * We put a limit parallelism on maximum number of partitions in local mode.
    * Local mode is used with limited memory.
    * With limited memory too large number of partitions will cause problems with GC.
    * 4 seems like a good maximum number of partitions for small datasets and 1GB memory.
    */
  private lazy val parallelism = {
    val availableCoresNumber: Int = Runtime.getRuntime.availableProcessors()
    math.min(availableCoresNumber, 4)
  }
}
