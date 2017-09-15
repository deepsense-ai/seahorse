/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.parameters

import spray.json.DefaultJsonProtocol._
import spray.json._

import io.deepsense.deeplang.parameters.ParameterType.ParameterType
import io.deepsense.deeplang.parameters.exceptions.ParameterRequiredException

/**
 * Holds parameter value.
 *
 * Parameters are used to fill parameter
 * schemas with their values and validate them.
 */
@SerialVersionUID(1)
abstract class Parameter extends Serializable {
  type HeldValue <: Any

  val parameterType: ParameterType

  val description: String

  /** Flag specifying if parameter is required. */
  val required: Boolean

  /** Value of parameter. */
  var value: Option[HeldValue] = None

  /**
   * Returns another parameter which has all fields equal to this parameter's fields
   * except for held value.
   */
  private[parameters] def replicate: Parameter

  /**
   * Validates held value.
   * If value is set to None and required, exception is thrown.
   */
  def validate: Unit = value match {
    case Some(definedValue) => validateDefined(definedValue)
    case None => if (required) {
      throw ParameterRequiredException(parameterType)
    }
  }

  /**
   * Place for additional validation in subclasses.
   * This validation is not performed if value is set to None.
   * This function does nothing by default.
   */
  protected def validateDefined(definedValue: HeldValue): Unit = { }

  /**
   * Map of fields that should be used in each parameter's Json representation.
   * ParametersSchema containing this parameter will use these fields along with parameters name
   * to describe its contents.
   */
  private[deeplang] def jsDescription: Map[String, JsValue] = {
    Map(
      "type" -> parameterType.toString.toJson,
      "description" -> description.toJson,
      "required" -> required.toJson)
  }

  /**
   * Json representation of value held by this parameter.
   * If it is not provided, it returns JsNull.
   */
  def valueToJson: JsValue = value match {
    case Some(definedValue) => definedValueToJson(definedValue)
    case None => JsNull
  }

  /**
   * Json representation of value held by parameter if this value is provided.
   */
  protected def definedValueToJson(definedValue: HeldValue): JsValue

  /**
   * Fills parameter with value basing on json representation of this value.
   */
  def fillValueWithJson(jsValue: JsValue): Unit = {
    value = jsValue match {
      case JsNull => None
      case _ => Some(valueFromDefinedJson(jsValue))
    }
  }

  /**
   * Returns value of parameter basing on json representation of this value.
   * Assumes that `jsValue` is not JsNull. Performs all required side effects of setting value.
   */
  protected def valueFromDefinedJson(jsValue: JsValue): HeldValue
}
