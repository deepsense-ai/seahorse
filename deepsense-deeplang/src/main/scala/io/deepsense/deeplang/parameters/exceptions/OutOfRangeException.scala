/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters.exceptions

case class OutOfRangeException(value: Double, lowerBound: Double, upperBound: Double)
  extends ValidationException(s"Parameter value is out of range. " +
    s"Value $value is not in [$lowerBound; $upperBound]")
