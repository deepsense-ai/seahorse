/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher

import java.util.logging.Level

import scala.concurrent.Future
import scalaz._

import com.google.inject.Inject
import org.apache.spark.launcher.SparkLauncher

import io.deepsense.commons.utils.Logging
import io.deepsense.sessionmanager.rest.requests.ClusterDetails
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters.SeahorseSparkLauncher
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.outputintercepting.OutputInterceptorFactory
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.{SparkAgumentParser, PromiseSparkAppHandle}
import io.deepsense.sessionmanager.service.sessionspawner.{SessionConfig, SessionSpawner}

class SparkLauncherSessionSpawner @Inject()(
  private val sparkLauncherConfig: SparkLauncherConfig,
  private val outputInterceptorFactory: OutputInterceptorFactory
) extends SessionSpawner with Logging {

  override def createSession(
      sessionConfig: SessionConfig,
      clusterDetails: ClusterDetails): Future[Unit] = {
    logger.info(s"Creating session for workflow ${sessionConfig.workflowId}")

    val interceptorHandle = outputInterceptorFactory.prepareInterceptorWritingToFiles(
      clusterDetails
    )

    SeahorseSparkLauncher(sessionConfig, sparkLauncherConfig, clusterDetails) match {
      case Success(sparkLauncher) =>
        val listener = new PromiseSparkAppHandle()
        interceptorHandle.attachTo(sparkLauncher)
        sparkLauncher.startApplication(listener)
        listener.executorStartedFuture
      case Failure(error) =>
        interceptorHandle.writeOutput(error.getMessage)
        Future.failed(error)
    }
  }

}
