/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.executor

import io.deepsense.sessionmanager.rest.requests.ClusterDetails
import io.deepsense.sessionmanager.service.sessionspawner.SessionConfig
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.SparkLauncherConfig

object LocalSessionExecutorArgs {

  // TODO Have args parser/formatter in we.jar and use it here to have type-safety

  def apply(
      sessionConfig: SessionConfig,
      config: SparkLauncherConfig,
      clusterConfig: ClusterDetails): Seq[String] = {
    // local cluster can access queue and wm under local host address
    val clusterConfigWithLocalhost = clusterConfig.copy(
      userIP = "127.0.0.1"
    )
    SessionExecutorArgs(sessionConfig, config, clusterConfigWithLocalhost)
  }
}
