/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.parameters

import spray.json._

case class BooleanParameter(
    description: String,
    default: Option[Boolean],
    required: Boolean)
  extends Parameter
  with CanHaveDefault[Boolean] {

  type HeldValue = Boolean

  val parameterType = ParameterType.Boolean

  var value: Option[Boolean] = None

  private[parameters] def replicate: Parameter = copy()

  override protected def defaultValueToJson(defaultValue: Boolean) = defaultValue.toJson

  override protected def definedValueToJson(definedValue: Boolean): JsValue = definedValue.toJson

  override protected def valueFromJsonPF: PartialFunction[JsValue, Unit] = {
    case JsNull => value = None
    case JsBoolean(x) => value = Some(x)
  }
}

case class NumericParameter(
    description: String,
    default: Option[Double],
    required: Boolean,
    validator: Validator[Double])
  extends Parameter
  with HasValidator
  with CanHaveDefault[Double] {

  type HeldValue = Double

  val parameterType = ParameterType.Numeric

  var value: Option[Double] = None

  private[parameters] def replicate: Parameter = copy()

  override protected def defaultValueToJson(defaultValue: Double): JsValue = defaultValue.toJson

  override protected def definedValueToJson(definedValue: Double): JsValue = definedValue.toJson

  override protected def valueFromJsonPF: PartialFunction[JsValue, Unit] = {
    case JsNull => value = None
    case JsNumber(x) => value = Some(x.toDouble)
    // TODO Here we convert from BigDecimal. Maybe we should throw some our exception here?
  }
}

case class StringParameter(
    description: String,
    default: Option[String],
    required: Boolean,
    validator: Validator[String])
  extends Parameter
  with HasValidator
  with CanHaveDefault[String] {

  type HeldValue = String

  val parameterType = ParameterType.String

  var value: Option[String] = None

  private[parameters] def replicate: Parameter = copy()

  override protected def defaultValueToJson(defaultValue: String): JsValue = defaultValue.toJson

  override protected def definedValueToJson(definedValue: String): JsValue = definedValue.toJson

  override protected def valueFromJsonPF: PartialFunction[JsValue, Unit] = {
    case JsNull => value = None
    case JsString(x) => value = Some(x)
  }
}

/**
 * Holds choice parameter - its possible values and chosen value.
 * Its value is one of possible choice values.
 * After the value is set to be one of the possible options,
 * its internal schema should be set to the schema of chosen
 * option. Therefore referential equality between chosen option
 * schema and one of the possible options schemas is assumed.
 * @param default label of option selected by default
 * @param options possible choices - their labels and schemas
 */
case class ChoiceParameter(
    description: String,
    default: Option[String],
    required: Boolean,
    options: Map[String, ParametersSchema])
  extends Parameter
  with HasChoice
  with CanHaveDefault[String] {

  type HeldValue = Selection

  val parameterType = ParameterType.Choice

  private var _value: Option[Selection] = None

  def value: Option[Selection] = _value

  override protected def validateDefined(definedValue: Selection): Unit = {
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

  override protected def defaultValueToJson(defaultValue: String): JsValue = defaultValue.toJson

  override protected def definedValueToJson(definedValue: Selection): JsValue = {
    JsObject(selectionToJson(definedValue))
  }

  override protected def valueFromJsonPF: PartialFunction[JsValue, Unit] = {
    case JsNull => _value = None
    case jsValue@JsObject(map) =>
      if (map.size != 1) {
        throw new DeserializationException(s"There should be only one selected option in choice" +
          s"parameter, but there are ${map.size} in $jsValue")
      }
      val (label, innerJsValue) = map.iterator.next()
      fill(label, schema => schema.fillValuesWithJson(innerJsValue))
  }
}

/**
 * Holds multiple choice parameter - its possible values and chosen values.
 * Its value is a set of chosen values.
 * After the value is set to be a set of the possible options,
 * its internal schemas should be equal to the schema of chosen
 * options. Therefore referential equality between chosen options
 * schemas and some of the possible options schemas (namely selected ones) is assumed.
 * @param default labels of options selected by default
 * @param options possible choices - their labels and schemas
 */
case class MultipleChoiceParameter(
    description: String,
    default: Option[Traversable[String]],
    required: Boolean,
    options: Map[String, ParametersSchema])
  extends Parameter
  with HasChoice
  with CanHaveDefault[Traversable[String]] {

  type HeldValue = MultipleSelection

  val parameterType = ParameterType.MultipleChoice

  private var _value: Option[MultipleSelection] = None

  def value: Option[MultipleSelection] = _value

  override protected def validateDefined(definedValue: MultipleSelection): Unit = {
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

  override protected def defaultValueToJson(defaultValue: Traversable[String]): JsValue = {
    defaultValue.toList.toJson
  }

  override protected def definedValueToJson(definedValue: MultipleSelection): JsValue = {
    val fields = for (selection <- definedValue.choices.toSeq) yield selectionToJson(selection)
    JsObject(fields: _*)
  }

  override protected def valueFromJsonPF: PartialFunction[JsValue, Unit] = {
    case JsNull => _value = None
    case JsObject(map) =>
      fill(map.mapValues(innerJsValue => _.fillValuesWithJson(innerJsValue)))
  }
}

/**
 * Value of this parameter is list of filled schemas which all conform to the predefined schema.
 * @param valuesSchema predefined schema that all schemas in value should conform to
 */
case class MultiplierParameter(
    description: String,
    required: Boolean,
    valuesSchema: ParametersSchema)
  extends Parameter {
  type HeldValue = Multiplied

  val parameterType = ParameterType.Multiplier

  private var _value: Option[Multiplied] = None

  def value: Option[Multiplied] = _value

  /** Validates each filled schema. */
  override protected def validateDefined(definedValue: Multiplied): Unit = {
    definedValue.schemas.foreach(_.validate)
  }

  /**
   * Fills this holder with values. Receives list of functions, which for given schema
   * can fill it with values. All schemas provided to fillers will by copies of valuesSchema.
   * @param fillers list of functions able to fill valuesSchema
   */
  def fill(fillers: Vector[ParametersSchema => Unit]) = {
    val filled = for (filler <- fillers) yield {
      val another = valuesSchema.replicate
      filler(another)
      another
    }
    _value = Some(Multiplied(filled))
  }

  private[parameters] def replicate: Parameter = copy()

  override def toJson: JsObject = {
    JsObject(basicJsonFields + ("values" -> valuesSchema.toJson))
  }

  override protected def definedValueToJson(definedValue: Multiplied): JsValue = {
    val fields = for (schema <- definedValue.schemas) yield schema.valueToJson
    JsArray(fields:_*)
  }

  private def fillerForJsValue(jsValue: JsValue)(schema: ParametersSchema): Unit = {
    schema.fillValuesWithJson(jsValue)
  }

  override protected def valueFromJsonPF: PartialFunction[JsValue, Unit] = {
    case JsNull => _value = None
    case JsArray(vector) => fill(vector.map(fillerForJsValue))
  }
}

/**
 * Abstract parameter that allows to select columns.
 */
abstract sealed class AbstractColumnSelectorParameter extends Parameter {
  val parameterType = ParameterType.ColumnSelector

  /** Tells if this selectors selects single column or many. */
  protected val isSingle: Boolean

  override def toJson: JsObject = {
    JsObject(basicJsonFields + ("isSingle" -> isSingle.toJson))
  }
}

/**
 * Holds value that points single column.
 */
case class SingleColumnSelectorParameter(
    description: String,
    required: Boolean)
  extends AbstractColumnSelectorParameter {
  type HeldValue = SingleColumnSelection

  protected val isSingle = true

  var value: Option[SingleColumnSelection] = None

  private[parameters] def replicate: Parameter = copy()

  override protected def definedValueToJson(definedValue: SingleColumnSelection): JsValue = {
    definedValue.toJson
  }

  override protected def valueFromJsonPF: PartialFunction[JsValue, Unit] = {
    case JsNull => value = None
    case x => value = Some(SingleColumnSelection.fromJson(x))
  }
}

/**
 * Holds value that points to multiple columns.
 */
case class ColumnSelectorParameter(
    description: String,
    required: Boolean)
  extends AbstractColumnSelectorParameter {
  type HeldValue = MultipleColumnSelection

  protected val isSingle = false

  var value: Option[MultipleColumnSelection] = None

  private[parameters] def replicate: Parameter = copy()

  override protected def definedValueToJson(definedValue: MultipleColumnSelection): JsValue = {
    val fields = definedValue.selections.map(_.toJson)
    JsArray(fields:_*)
  }

  override protected def valueFromJsonPF: PartialFunction[JsValue, Unit] = {
    case JsNull => value = None
    case x => value = Some(MultipleColumnSelection.fromJson(x))
  }
}
