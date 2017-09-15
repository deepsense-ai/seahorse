/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

import io.deepsense.deeplang.parameters.exceptions.OutOfRangeException

/**
 * Validates if NumericParameter value is within range bounds.
 * Both ends of range are inclusive.
 */
case class RangeValidator(
    lowerBound: Double,
    higherBound: Double) extends Validator[NumericParameter] {
  val validatorType = ValidatorType.Range

  override def validate(parameter: NumericParameter): Unit = {
    if (parameter.value < lowerBound || parameter.value > higherBound) {
      throw new OutOfRangeException(parameter.value, lowerBound, higherBound)
    }
  }
}