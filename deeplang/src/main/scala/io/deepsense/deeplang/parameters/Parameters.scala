/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.parameters

import scala.collection.immutable.ListMap

import spray.json.DefaultJsonProtocol._
import spray.json._

case class BooleanParameter(
    description: String,
    default: Option[Boolean],
    required: Boolean)
  extends Parameter
  with CanHaveDefault {

  type HeldValue = Boolean

  val parameterType = ParameterType.Boolean

  private[parameters] def replicate: Parameter = copy()

  override protected def defaultValueToJson(defaultValue: Boolean) = defaultValue.toJson

  override protected def definedValueToJson(definedValue: Boolean): JsValue = definedValue.toJson

  override protected def valueFromDefinedJson(jsValue: JsValue): Boolean = {
    jsValue.convertTo[Boolean]
  }
}

case class NumericParameter(
    description: String,
    default: Option[Double],
    required: Boolean,
    validator: Validator[Double])
  extends Parameter
  with HasValidator
  with CanHaveDefault {

  type HeldValue = Double

  val parameterType = ParameterType.Numeric

  private[parameters] def replicate: Parameter = copy()

  override protected def defaultValueToJson(defaultValue: Double): JsValue = defaultValue.toJson

  override protected def definedValueToJson(definedValue: Double): JsValue = definedValue.toJson

  override protected def valueFromDefinedJson(jsValue: JsValue): Double = {
    jsValue.convertTo[Double]
  }
}

case class StringParameter(
    description: String,
    default: Option[String],
    required: Boolean,
    validator: Validator[String])
  extends Parameter
  with HasValidator
  with CanHaveDefault {

  type HeldValue = String

  val parameterType = ParameterType.String

  private[parameters] def replicate: Parameter = copy()

  override protected def defaultValueToJson(defaultValue: String): JsValue = defaultValue.toJson

  override protected def definedValueToJson(definedValue: String): JsValue = definedValue.toJson

  override protected def valueFromDefinedJson(jsValue: JsValue): String = {
    jsValue.convertTo[String]
  }
}

/**
 * Holds choice parameter - its possible values and chosen value.
 * Its value is label pointing to one of possible choice values.
 * @param default label of option selected by default
 * @param options possible choices - their labels and schemas
 */
case class ChoiceParameter(
    description: String,
    default: Option[String],
    required: Boolean,
    options: ListMap[String, ParametersSchema])
  extends Parameter
  with HasOptions
  with CanHaveDefault {

  type HeldValue = String

  val parameterType = ParameterType.Choice

  override protected def validateDefined(definedValue: String): Unit = {
    validateChoice(definedValue)
  }

  private[parameters] def replicate: Parameter = copy()

  override protected def defaultValueToJson(defaultValue: String): JsValue = defaultValue.toJson

  override protected def definedValueToJson(definedValue: String): JsValue = {
    JsObject(choiceToJson(definedValue))
  }

  /**
   * Side effect of this function is filling selected schemas with corresponding json.
   */
  override protected def valueFromDefinedJson(jsValue: JsValue): String = jsValue match {
    case JsObject(map) =>
      if (map.size != 1) {
        throw new DeserializationException(s"There should be only one selected option in choice" +
          s"parameter, but there are ${map.size} in $jsValue.")
      }
      val (label, innerJsValue) = map.iterator.next()
      choiceFromJson(label, innerJsValue)
      label
    case _ => throw new DeserializationException(s"Cannot fill choice parameter with $jsValue:" +
      s"object expected.")
  }

  def selection: Option[Selection] = value.map(selectionForChoice)
}

/**
 * Holds multiple choice parameter - its possible values and chosen values.
 * Its value is a set of chosen labels.
 * @param default labels of options selected by default
 * @param options possible choices - their labels and schemas
 */
case class MultipleChoiceParameter(
    description: String,
    default: Option[Traversable[String]],
    required: Boolean,
    options: ListMap[String, ParametersSchema])
  extends Parameter
  with HasOptions
  with CanHaveDefault {

  type HeldValue = Traversable[String]

  val parameterType = ParameterType.MultipleChoice

  override protected def validateDefined(definedValue: Traversable[String]): Unit = {
    for (choice <- definedValue) {
      validateChoice(choice)
    }
  }

  private[parameters] def replicate: Parameter = copy()

  override protected def defaultValueToJson(defaultValue: Traversable[String]): JsValue = {
    defaultValue.toList.toJson
  }

  override protected def definedValueToJson(definedValue: Traversable[String]): JsValue = {
    val fields = for (choice <- definedValue.toSeq) yield choiceToJson(choice)
    JsObject(fields: _*)
  }

  /**
   * Side effect of this function is filling selected schemas with corresponding json.
   */
  override protected def valueFromDefinedJson(jsValue: JsValue): Traversable[String] = {
    jsValue match {
      case JsObject(map) =>
        for ((label, innerJsValue) <- map) yield {
          choiceFromJson(label, innerJsValue)
          label
        }
      case _ => throw new DeserializationException(s"Cannot fill multiple choice parameter" +
        s"with $jsValue: object expected.")
    }
  }

  def selections: Option[Traversable[Selection]] = {
    value.map(labels => labels.map(selectionForChoice))
  }
}

/**
 * Value of this parameter is list of filled schemas which all conform to the predefined schema.
 * @param predefinedSchema predefined schema that all schemas in value should conform to
 */
case class ParametersSequence(
    description: String,
    required: Boolean,
    predefinedSchema: ParametersSchema)
  extends Parameter {
  type HeldValue = Vector[ParametersSchema]

  val parameterType = ParameterType.Multiplier

  /**
   * Validates each filled schema.
   * Does not check if all filled schemas conform to predefined schema.
   */
  override protected def validateDefined(definedValue: Vector[ParametersSchema]): Unit = {
    definedValue.foreach(_.validate)
  }

  private[parameters] def replicate: Parameter = copy()

  override def jsDescription: Map[String, JsValue] =
    super.jsDescription + ("values" -> predefinedSchema.toJson)

  override protected def definedValueToJson(definedValue: Vector[ParametersSchema]): JsValue = {
    val fields = for (schema <- definedValue) yield schema.valueToJson
    JsArray(fields:_*)
  }

  /**
   * Side effect of this function is creating schemas conforming to predefined schema
   * and filling them with corresponding json.
   */
  override protected def valueFromDefinedJson(jsValue: JsValue): Vector[ParametersSchema] = {
    jsValue match {
      case JsArray(vector) =>
        for (innerJsValue <- vector) yield {
          val replicatedSchema = predefinedSchema.replicate
          replicatedSchema.fillValuesWithJson(innerJsValue)
          replicatedSchema
        }
      case _ => throw new DeserializationException(s"Cannot fill parameters sequence" +
        s"with $jsValue: array expected.")
    }
  }
}

/**
 * Abstract parameter that allows to select columns.
 */
abstract sealed class AbstractColumnSelectorParameter extends Parameter {
  val parameterType = ParameterType.ColumnSelector

  /** Tells if this selectors selects single column or many. */
  protected val isSingle: Boolean

  override def jsDescription: Map[String, JsValue] =
    super.jsDescription + ("isSingle" -> isSingle.toJson)
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

  private[parameters] def replicate: Parameter = copy()

  override protected def definedValueToJson(definedValue: SingleColumnSelection): JsValue = {
    definedValue.toJson
  }

  override protected def valueFromDefinedJson(jsValue: JsValue): SingleColumnSelection = {
    SingleColumnSelection.fromJson(jsValue)
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

  private[parameters] def replicate: Parameter = copy()

  override protected def definedValueToJson(definedValue: MultipleColumnSelection): JsValue = {
    val fields = definedValue.selections.map(_.toJson)
    JsArray(fields:_*)
  }

  override protected def valueFromDefinedJson(jsValue: JsValue): MultipleColumnSelection = {
    MultipleColumnSelection.fromJson(jsValue)
  }

  override protected def validateDefined(definedValue: HeldValue): Unit = {
    definedValue.validate
  }
}
