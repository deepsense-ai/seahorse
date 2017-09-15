/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

/**
 * Represents ParameterHolder with validator.
 */
trait HasValidator extends ParameterHolder {
  val validator: Validator[HeldParameter]

  override def validateDefined(definedValue: HeldParameter) = {
    validator.validate(definedValue)
  }
}
