/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark

import scala.concurrent.{ExecutionContext, Future, Promise}

import org.apache.spark.launcher.SparkAppHandle
import org.apache.spark.launcher.SparkAppHandle.State

import io.deepsense.commons.utils.Logging

class LoggingSparkAppListener extends SparkAppHandle.Listener with Logging {

  override def infoChanged(handle: SparkAppHandle): Unit = {
    logger.info(s"App ${handle.getAppId} info changed: ${handle.toString}")
  }

  override def stateChanged(handle: SparkAppHandle): Unit = {
    logger.info(s"App ${handle.getAppId} state changed: ${handle.getState}")
  }
}
