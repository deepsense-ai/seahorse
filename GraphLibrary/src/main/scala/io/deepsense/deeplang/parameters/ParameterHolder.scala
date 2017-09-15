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
  /** Value of the held parameter. */
  def value: Option[HeldParameter]

  /**
   * Returns another holder which has all fields equal to this holder's fields
   * except for held value.
   */
  private[parameters] def replicate: ParameterHolder

  def validate: Unit = {
    if (!value.isDefined && required) {
      throw ParameterRequiredException(parameterType)
    }
  }
}
