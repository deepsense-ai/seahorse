/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

case class BooleanParameterHolder(
    description: String,
    default: Option[Boolean],
    required: Boolean)
  extends ParameterHolder {
  type HeldParameter = Boolean
  val parameterType = ParameterType.Boolean
  var value: Option[Boolean] = None

  private[parameters] def replicate: ParameterHolder = copy()
}

case class NumericParameterHolder(
    description: String,
    default: Option[Double],
    required: Boolean,
    validator: Validator[Double])
  extends ParameterHolder
  with HasValidator {
  type HeldParameter = Double
  val parameterType = ParameterType.Numeric
  var value: Option[Double] = None

  private[parameters] def replicate: ParameterHolder = copy()
}

case class StringParameterHolder(
    description: String,
    default: Option[String],
    required: Boolean,
    validator: Validator[String])
  extends ParameterHolder
  with HasValidator {
  type HeldParameter = String
  val parameterType = ParameterType.String
  var value: Option[String] = None

  private[parameters] def replicate: ParameterHolder = copy()
}

/**
 * Holds choice parameter - its possible values and chosen value.
 * Its value is one of possible choice values.
 * After the value is set to be one of the possible options,
 * its internal schema should be set to the schema of chosen
 * option. Therefore referential equality between chosen option
 * schema and one of the possible options schemas is assumed.
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
  private var _value: Option[ChoiceParameter] = None

  def value: Option[ChoiceParameter] = _value

  override def validateDefined(definedValue: ChoiceParameter): Unit = {
    validateChoices(Traversable(definedValue))
  }

  /**
   * Fills this holder with value. Label tells which option is chosen. Filler is function
   * that is able to fill selected schema with values. If selected label does not exist
   * in options, IllegalChoiceException is thrown.
   * @param label label of option that is chosen
   * @param filler function able to fill selected option schema
   */
  def fill(label: String, filler: ParametersSchema => Unit) = {
    _value = Some(fillChosen(Map(label -> filler)).head)
  }

  private[parameters] def replicate: ParameterHolder = copy()
}

/**
 * Holds multiple choice parameter - its possible values and chosen values.
 * Its value is a set of chosen values.
 * After the value is set to be a set of the possible options,
 * its internal schemas should be equal to the schema of chosen
 * options. Therefore referential equality between chosen options
 * schemas and some of the possible options schemas (namely selected ones) is assumed.
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
  private var _value: Option[MultipleChoiceParameter] = None

  def value: Option[MultipleChoiceParameter] = _value

  override def validateDefined(definedValue: MultipleChoiceParameter): Unit = {
    validateChoices(definedValue.choices)
  }

  /**
   * Fills this parameter with values. Receives map from label to filling function.
   * Each filling function has to be able to fill schema associated with its label.
   * If some label from fillers map does not exist in options, IllegalChoiceException is thrown.
   * @param fillers map from labels to filling functions
   */
  def fill(fillers: Map[String, ParametersSchema => Unit]) = {
    _value = Some(MultipleChoiceParameter(fillChosen(fillers)))
  }

  private[parameters] def replicate: ParameterHolder = copy()
}

/**
 * Holds multiplicator parameter - schema of its values and value list.
 * Its value is a list of filled schemas which all conform to the given values schema.
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
  private var _value: Option[MultiplicatorParameter] = None

  def value: Option[MultiplicatorParameter] = _value

  /** Validates each filled schema. */
  override def validateDefined(definedValue: MultiplicatorParameter): Unit = {
    definedValue.schemas.foreach(_.validate)
  }

  /**
   * Fills this holder with values. Receives list of functions, which for given schema
   * can fill it with values. All schemas provided to fillers will by copies of valuesSchema.
   * @param fillers list of functions able to fill valuesSchema
   */
  def fill(fillers: List[ParametersSchema => Unit]) = {
    val filled = for (filler <- fillers) yield {
      val another = valuesSchema.replicate
      filler(another)
      another
    }
    _value = Some(MultiplicatorParameter(filled))
  }

  private[parameters] def replicate: ParameterHolder = copy()
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
  var value: Option[SingleColumnSelection] = None

  private[parameters] def replicate: ParameterHolder = copy()
}

/**
 * Holds parameter that allows to select columns.
 */
case class MultipleColumnSelectorParameterHolder(
    description: String,
    required: Boolean)
  extends ColumnSelectorParameterHolder {
  type HeldParameter = MultipleColumnSelection
  var value: Option[MultipleColumnSelection] = None

  private[parameters] def replicate: ParameterHolder = copy()
}
