/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

/**
 * Represents ParameterHolder with validator.
 */
trait HasValidator extends Parameter {
  val validator: Validator[HeldValue]

  override def validateDefined(definedValue: HeldValue) = {
    validator.validate(definedValue)
  }
}
