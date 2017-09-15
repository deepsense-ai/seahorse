/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

case class BooleanParameterHolder(
    description: String,
    default: Option[BooleanParameter],
    required: Boolean)
  extends ParameterHolder {
  type HeldParameter = BooleanParameter

  val parameterType = ParameterType.Boolean
}

object BooleanParameterHolder {
  def apply(
      description: String,
      default: Boolean,
      required: Boolean): BooleanParameterHolder = {
    BooleanParameterHolder(description, Some(BooleanParameter(default)), required)
  }
}

case class NumericParameterHolder(
    description: String,
    default: Option[NumericParameter],
    required: Boolean,
    validator: Validator[NumericParameter])
  extends ParameterHolder
  with HasValidator {
  type HeldParameter = NumericParameter

  val parameterType = ParameterType.Numeric
}

object NumericParameterHolder {
  def apply(
      description: String,
      default: Double,
      required: Boolean,
      validator: Validator[NumericParameter]): NumericParameterHolder = {
    NumericParameterHolder(description, Some(NumericParameter(default)), required, validator)
  }
}

case class StringParameterHolder(
    description: String,
    default: Option[StringParameter],
    required: Boolean)
  extends ParameterHolder {
  type HeldParameter = StringParameter

  val parameterType = ParameterType.String
}

object StringParameterHolder {
  def apply(
      description: String,
      default: String,
      required: Boolean): StringParameterHolder = {
    StringParameterHolder(description, Some(StringParameter(default)), required)
  }
}
