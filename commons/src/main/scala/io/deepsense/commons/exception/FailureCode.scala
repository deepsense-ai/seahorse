/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.exception

object FailureCode extends Enumeration {
  type FailureCode = Value

  val NodeFailure = Value(1)
  val LaunchingFailure = Value(2)
  val ExperimentNotFound = Value(3)
  val CannotUpdateRunningExperiment = Value(4)
  val EntityNotFound = Value(5)
  val UnexpectedError = Value(6)
  val IllegalArgumentException = Value(7)

  def fromCode(code: Int): Option[FailureCode] = FailureCode.values.find(_.id == code)
}
