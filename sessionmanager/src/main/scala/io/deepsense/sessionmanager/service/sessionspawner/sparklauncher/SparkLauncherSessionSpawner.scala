/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._
import scalaz._
import scalaz.Validation.FlatMap._

import com.google.inject.Inject
import org.apache.spark.launcher.{SparkAppHandle, SparkLauncher}

import io.deepsense.commons.utils.Logging
import io.deepsense.commons.models.ClusterDetails
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters.SeahorseSparkLauncher
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.outputintercepting.OutputInterceptorFactory
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.LoggingSparkAppListener
import io.deepsense.sessionmanager.service.sessionspawner.{SessionConfig, SessionSpawner}

class SparkLauncherSessionSpawner @Inject()(
  private val sparkLauncherConfig: SparkLauncherConfig,
  private val outputInterceptorFactory: OutputInterceptorFactory
) extends SessionSpawner with Logging {

  override def createSession(
      sessionConfig: SessionConfig,
      clusterDetails: ClusterDetails): Validation[SparkLauncherError, SparkAppHandle] = {
    logger.info(s"Creating session for workflow ${sessionConfig.workflowId}")

    val interceptorHandle = outputInterceptorFactory.prepareInterceptorWritingToFiles(
      clusterDetails
    )

    val startedApplicationHandler = for {
      launcher <- SeahorseSparkLauncher(sessionConfig, sparkLauncherConfig, clusterDetails)
      handleForStartedApplication <- handleUnexpectedExceptions {
        interceptorHandle.attachTo(launcher)
        launcher.startApplication(new LoggingSparkAppListener())
      }
    } yield handleForStartedApplication

    startedApplicationHandler.bimap(error => {
      interceptorHandle.writeOutput(error.getMessage)
      error
    }, identity)
  }

  private def handleUnexpectedExceptions[T, E <: SparkLauncherError]
      (code: => T): Validation[UnexpectedException, T] =
    try {
      code.success
    } catch {
      case ex: Exception => UnexpectedException(ex).failure
    }

}
