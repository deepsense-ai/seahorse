/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.exceptions

/**
 * Collection of Error codes in Experiment Manager
 */
// TODO: This class should ensure that each error code is unique.
// Please consult spray.http.StatusCodes for details
object ErrorCodes {
  val ExperimentNotFound = 5
  val ExperimentRunning = 10
  // val SomeOtherCode = 10 // TODO
}
