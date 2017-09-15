/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

import io.deepsense.deeplang.parameters.exceptions.IllegalChoiceException

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
    required: Boolean,
    validator: Validator[StringParameter])
  extends ParameterHolder
  with HasValidator {
  type HeldParameter = StringParameter

  val parameterType = ParameterType.String
}

object StringParameterHolder {
  def apply(
      description: String,
      default: String,
      required: Boolean,
      validator: Validator[StringParameter]): StringParameterHolder = {
    StringParameterHolder(description, Some(StringParameter(default)), required, validator)
  }
}

/**
 * Holds choice parameter - its possible values and chosen value.
 * Its value is one of possible choice values.
 * After the value is set to be one of the possible options,
 * its internal schema should be set to the schema of chosen
 * option. Therefore referential equality between chosen option
 * schema and one of the possible options schemas is assumed.
 *
 * @param options possible choices - their labels and schemas
 */
case class ChoiceParameterHolder(
    description: String,
    default: Option[ChoiceParameter],
    required: Boolean,
    options: Map[String, ParametersSchema])
  extends ParameterHolder
  with HasChoice {
  type HeldParameter = ChoiceParameter

  val parameterType = ParameterType.Choice

  override def validate: Unit = {
    super.validate
    validateChoices(Set(value.get))
  }
}

/**
 * Holds multiple choice parameter - its possible values and chosen values.
 * Its value is a set of chosen values.
 * After the value is set to be a set of the possible options,
 * its internal schemas should be equal to the schema of chosen
 * options. Therefore referential equality between chosen options
 * schemas and some of the possible options schemas (namely selected ones) is assumed.
 *
 * @param options possible choices - their labels and schemas
 */
case class MultipleChoiceParameterHolder(
    description: String,
    default: Option[MultipleChoiceParameter],
    required: Boolean,
    options: Map[String, ParametersSchema])
  extends ParameterHolder
  with HasChoice {
  type HeldParameter = MultipleChoiceParameter

  val parameterType = ParameterType.MultipleChoice

  override def validate: Unit = {
    super.validate
    validateChoices(value.get.values)
  }
}
