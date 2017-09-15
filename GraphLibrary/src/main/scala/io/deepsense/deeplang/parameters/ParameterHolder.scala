/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

import io.deepsense.deeplang.parameters.ParameterType.ParameterType
import io.deepsense.deeplang.parameters.exceptions.ParameterRequiredException

/**
 * Holds parameter value and is able to validate it.
 */
abstract class ParameterHolder {
  type HeldParameter <: Parameter

  val parameterType: ParameterType
  val description: String
  val default: Option[HeldParameter]
  val required: Boolean
  private[parameters] var value: Option[HeldParameter] = None

  def validate: Unit = {
    if (!value.isDefined && required) {
      throw ParameterRequiredException(parameterType)
    }
  }
}
