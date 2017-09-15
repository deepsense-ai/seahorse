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

/**
 * Holds multiplicator parameter - schema of its values and value list.
 * Its value is a list of filled schemas which all conform to the given values schema.
 *
 * @param valuesSchema schema of the values of the parameter
 */
case class MultiplicatorParameterHolder(
    description: String,
    default: Option[MultiplicatorParameter],
    required: Boolean,
    valuesSchema: ParametersSchema)
  extends ParameterHolder {
  type HeldParameter = MultiplicatorParameter

  val parameterType = ParameterType.Multiplicator

  /** Validates each filled schema with parameters */
  override def validate: Unit = {
    super.validate
    value.get.value.foreach(_.validate)
  }
}

abstract class ColumnSelectorParameterHolder extends ParameterHolder {
  val default = None
  val parameterType = ParameterType.ColumnSelector
}

/**
 * Holds parameter that allows to select single column.
 */
case class SingleColumnSelectorParameterHolder(
    description: String,
    required: Boolean)
  extends ColumnSelectorParameterHolder {
  type HeldParameter = SingleColumnSelection
}

/**
 * Holds parameter that allows to select columns.
 */
case class MultipleColumnSelectorParameterHolder(
    description: String,
    required: Boolean)
  extends ColumnSelectorParameterHolder {
  type HeldParameter = MultipleColumnSelection
}
