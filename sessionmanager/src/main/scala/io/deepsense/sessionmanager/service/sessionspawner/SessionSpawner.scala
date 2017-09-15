/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner

import scalaz.Validation

import org.apache.spark.launcher.SparkAppHandle

import io.deepsense.commons.models.ClusterDetails
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.SparkLauncherError

trait SessionSpawner {
  def createSession(
    sessionConfig: SessionConfig,
    clusterConfig: ClusterDetails): Validation[SparkLauncherError, SparkAppHandle]
}
