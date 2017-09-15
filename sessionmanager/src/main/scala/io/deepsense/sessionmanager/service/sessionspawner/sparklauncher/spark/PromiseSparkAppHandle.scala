/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark

import scala.concurrent.{Future, Promise}

import org.apache.spark.launcher.SparkAppHandle

import io.deepsense.commons.utils.Logging
import io.deepsense.sessionmanager.service.sessionspawner.SessionSpawnerException

class PromiseSparkAppHandle extends SparkAppHandle.Listener with Logging {

  def executorStartedFuture: Future[Unit] = promise.future

  private val promise = Promise[Unit]

  override def infoChanged(handle: SparkAppHandle): Unit = {
    logger.info(s"App ${handle.getAppId} info changed: ${handle.toString}")
  }

  override def stateChanged(handle: SparkAppHandle): Unit = {
    logger.info(s"App ${handle.getAppId} state changed: ${handle.getState}")
    if (!promise.isCompleted) {
      handle.getState match {
        case SparkAppHandle.State.SUBMITTED => promise.success(())
        case SparkAppHandle.State.FAILED | SparkAppHandle.State.KILLED => {
          val msg = s"Spark process could not start. State: ${handle.getState} "
          val exception = new SessionSpawnerException(msg)
          promise.failure(exception)
        }
        case other =>
      }
    }
  }
}
