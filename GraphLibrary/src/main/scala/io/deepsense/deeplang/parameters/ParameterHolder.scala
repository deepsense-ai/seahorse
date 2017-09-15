/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

import io.deepsense.deeplang.parameters.ParameterType.ParameterType
import io.deepsense.deeplang.parameters.exceptions.ParameterRequiredException

/**
 * Holds parameter value.
 *
 * ParameterHolders are used to fill parameter
 * schemas with their values and validate them.
 */
abstract class ParameterHolder {
  type HeldParameter <: Parameter

  val parameterType: ParameterType
  val description: String
  /** Default value of the parameter. Can be None if not provided. */
  val default: Option[HeldParameter]
  /** Flag specifying if parameter is required. */
  val required: Boolean
  /** Value of the held parameter. Can be injected after creation of the holder. */
  private[parameters] var value: Option[HeldParameter] = None

  def validate: Unit = {
    if (!value.isDefined && required) {
      throw ParameterRequiredException(parameterType)
    }
  }
}
