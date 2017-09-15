/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

import io.deepsense.deeplang.parameters.ValidatorType.ValidatorType

/** Represents anything that validates parameter. */
trait Validator[ParameterType] {
  val validatorType: ValidatorType

  def validate(parameter: ParameterType)
}
