/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

case class BooleanParameter(
    description: String,
    default: Option[Boolean],
    required: Boolean)
  extends Parameter {
  type HeldValue = Boolean

  val parameterType = ParameterType.Boolean

  var value: Option[Boolean] = None

  private[parameters] def replicate: Parameter = copy()
}

case class NumericParameter(
    description: String,
    default: Option[Double],
    required: Boolean,
    validator: Validator[Double])
  extends Parameter
  with HasValidator {
  type HeldValue = Double

  val parameterType = ParameterType.Numeric

  var value: Option[Double] = None

  private[parameters] def replicate: Parameter = copy()
}

case class StringParameter(
    description: String,
    default: Option[String],
    required: Boolean,
    validator: Validator[String])
  extends Parameter
  with HasValidator {
  type HeldValue = String

  val parameterType = ParameterType.String

  var value: Option[String] = None

  private[parameters] def replicate: Parameter = copy()
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
case class ChoiceParameter(
    description: String,
    default: Option[Selection],
    required: Boolean,
    options: Map[String, ParametersSchema])
  extends Parameter
  with HasChoice {
  type HeldValue = Selection

  val parameterType = ParameterType.Choice

  private var _value: Option[Selection] = None

  def value: Option[Selection] = _value

  override def validateDefined(definedValue: Selection): Unit = {
    validateChoices(Traversable(definedValue))
  }

  /**
   * Fills this parameter with value. Label tells which option is chosen. Filler is function
   * that is able to fill selected schema with values. If selected label does not exist
   * in options, IllegalChoiceException is thrown.
   * @param label label of option that is chosen
   * @param filler function able to fill selected option schema
   */
  def fill(label: String, filler: ParametersSchema => Unit) = {
    _value = Some(fillChosen(Map(label -> filler)).head)
  }

  private[parameters] def replicate: Parameter = copy()
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
case class MultipleChoiceParameter(
    description: String,
    default: Option[MultipleSelection],
    required: Boolean,
    options: Map[String, ParametersSchema])
  extends Parameter
  with HasChoice {
  type HeldValue = MultipleSelection

  val parameterType = ParameterType.MultipleChoice

  private var _value: Option[MultipleSelection] = None

  def value: Option[MultipleSelection] = _value

  override def validateDefined(definedValue: MultipleSelection): Unit = {
    validateChoices(definedValue.choices)
  }

  /**
   * Fills this parameter with values. Receives map from label to filling function.
   * Each filling function has to be able to fill schema associated with its label.
   * If some label from fillers map does not exist in options, IllegalChoiceException is thrown.
   * @param fillers map from labels to filling functions
   */
  def fill(fillers: Map[String, ParametersSchema => Unit]) = {
    _value = Some(MultipleSelection(fillChosen(fillers)))
  }

  private[parameters] def replicate: Parameter = copy()
}

/**
 * Value of this parameter is list of filled schemas which all conform to the predefined schema.
 * @param valuesSchema predefined schema that all schemas in value should conform to
 */
case class MultiplierParameter(
    description: String,
    default: Option[Multiplied],
    required: Boolean,
    valuesSchema: ParametersSchema)
  extends Parameter {
  type HeldValue = Multiplied

  val parameterType = ParameterType.Multiplicator

  private var _value: Option[Multiplied] = None

  def value: Option[Multiplied] = _value

  /** Validates each filled schema. */
  override def validateDefined(definedValue: Multiplied): Unit = {
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
    _value = Some(Multiplied(filled))
  }

  private[parameters] def replicate: Parameter = copy()
}

/**
 * Abstract parameter that allows to select columns.
 */
abstract class AbstractColumnSelectorParameter extends Parameter {
  val default: Option[HeldValue] = None

  val parameterType = ParameterType.ColumnSelector
}

/**
 * Holds value that points single column.
 */
case class SingleColumnSelectorParameter(
    description: String,
    required: Boolean)
  extends AbstractColumnSelectorParameter {
  type HeldValue = SingleColumnSelection

  var value: Option[SingleColumnSelection] = None

  private[parameters] def replicate: Parameter = copy()
}

/**
 * Holds value that points to multiple columns.
 */
case class ColumnSelectorParameter(
    description: String,
    required: Boolean)
  extends AbstractColumnSelectorParameter {
  type HeldValue = MultipleColumnSelection

  var value: Option[MultipleColumnSelection] = None

  private[parameters] def replicate: Parameter = copy()
}
