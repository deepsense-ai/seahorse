/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher

abstract class SparkLauncherError(message: String, throwable: Throwable)
  extends Exception(message, throwable) {

  def this(message: String) = {
    this(message, null)
  }
}

case class UnexpectedException(throwable: Throwable) extends SparkLauncherError(
  "Unexpected exception: " + throwable.getMessage, throwable
)
