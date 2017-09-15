/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher

import scala.concurrent.Future

import com.google.inject.Inject

import io.deepsense.commons.utils.Logging
import io.deepsense.sessionmanager.rest.requests.ClusterDetails
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters.SeahorseSparkLauncher
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.PromiseSparkAppHandle
import io.deepsense.sessionmanager.service.sessionspawner.{SessionConfig, SessionSpawner}

class SparkLauncherSessionSpawner @Inject()(
  private val sparkLauncherConfig: SparkLauncherConfig
) extends SessionSpawner with Logging {

  override def createSession(
      sessionConfig: SessionConfig,
      clusterConfig: ClusterDetails): Future[Unit] = {
    logger.info(s"Creating session for workflow ${sessionConfig.workflowId}")

    val sparkLauncher = SeahorseSparkLauncher(sessionConfig, sparkLauncherConfig, clusterConfig)

    val listener = new PromiseSparkAppHandle()
    sparkLauncher.startApplication(listener)
    listener.executorStartedFuture
  }

}
