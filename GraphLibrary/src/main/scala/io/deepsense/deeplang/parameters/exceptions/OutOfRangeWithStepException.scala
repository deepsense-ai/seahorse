/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters.exceptions

case class OutOfRangeWithStepException(
    value: Double,
    lowerBound: Double,
    upperBound: Double,
    step: Double)
  extends ValidationException(s"Parameter value is out of range. " +
    s"Value $value is not in [$lowerBound; $upperBound] with step $step")
