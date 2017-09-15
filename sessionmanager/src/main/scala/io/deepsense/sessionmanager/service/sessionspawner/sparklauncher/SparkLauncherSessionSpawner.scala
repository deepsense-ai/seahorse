/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher

import java.time.Instant

import scalaz.Scalaz._
import scalaz.Validation.FlatMap._
import scalaz._

import com.google.inject.Inject

import io.deepsense.commons.models.ClusterDetails
import io.deepsense.commons.utils.Logging
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters.SeahorseSparkLauncher
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.outputintercepting.OutputInterceptorFactory
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.LoggingSparkAppListener
import io.deepsense.sessionmanager.service.sessionspawner.{ExecutorSession, SessionConfig, SessionSpawner, StateInferencerFactory}

class SparkLauncherSessionSpawner @Inject()(
  private val sparkLauncherConfig: SparkLauncherConfig,
  private val outputInterceptorFactory: OutputInterceptorFactory,
  private val stateInferencerFactory: StateInferencerFactory
) extends SessionSpawner with Logging {

  override def createSession(
      sessionConfig: SessionConfig,
      clusterDetails: ClusterDetails): ExecutorSession = {
    logger.info(s"Creating session for workflow ${sessionConfig.workflowId}")

    val interceptorHandle = outputInterceptorFactory.prepareInterceptorWritingToFiles(
      clusterDetails
    )

    val startedSession = for {
      launcher <- SeahorseSparkLauncher(sessionConfig, sparkLauncherConfig, clusterDetails)
      listener = new LoggingSparkAppListener()
      handle <- handleUnexpectedExceptions {
        interceptorHandle.attachTo(launcher)
        launcher.startApplication(listener)
      }
      stateInferencer = stateInferencerFactory.newInferencer(Instant.now())
      executorSession = ExecutorSession(
        sessionConfig, clusterDetails, Some(handle), stateInferencer, interceptorHandle
      )
    } yield executorSession

    startedSession.fold(error => {
      interceptorHandle.writeOutput(error.getMessage)
      val stateInferencer = stateInferencerFactory.newInferencer(Instant.now())
      ExecutorSession(sessionConfig, clusterDetails, None, stateInferencer, interceptorHandle)
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
