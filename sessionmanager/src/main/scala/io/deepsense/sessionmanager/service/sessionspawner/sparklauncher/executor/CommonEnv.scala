/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.executor

import io.deepsense.sessionmanager.rest.requests.ClusterDetails
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.SparkLauncherConfig

object CommonEnv {

  def apply(config: SparkLauncherConfig,
            clusterConfig: ClusterDetails): Map[String, String] = Map(
    "HADOOP_USER_NAME" -> clusterConfig.hadoopUser.getOrElse("root"),
    "PYSPARK_DRIVER_PYTHON" -> config.pythonDriverBinary,
    "PYSPARK_PYTHON" -> config.pythonExecutorBinary
  )

}
