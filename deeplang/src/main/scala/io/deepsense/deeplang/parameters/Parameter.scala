/**
 * Copyright 2015, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.deeplang.parameters

import io.deepsense.deeplang.exceptions.DeepLangException
import io.deepsense.deeplang.parameters.exceptions.ParameterRequiredException

import spray.json.DefaultJsonProtocol._
import spray.json._

import io.deepsense.deeplang.parameters.ParameterType.ParameterType

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

  /** Value of parameter. */
  protected var _value: Option[HeldValue]

  def value: HeldValue = _value.get

  def maybeValue: Option[HeldValue] = _value

  def value_= (value: Option[HeldValue]): Unit = _value = value

  def value_= (value: HeldValue): Unit = _value = Some(value)

  /**
   * Returns another parameter which has all fields equal to this parameter's fields
   * except for held value.
   */
  private[parameters] def replicate: Parameter

  /**
   * Validates held value.
   * If value is set to None exception is thrown.
   */
  def validate(parameterName: String): Vector[DeepLangException] = maybeValue match {
    case Some(definedValue) => validateDefined(definedValue)
    case None => Vector(ParameterRequiredException(parameterName))
  }

  /**
   * Place for additional validation in subclasses.
   * This validation is not performed if value is set to None.
   * This function does nothing by default.
   */
  protected def validateDefined(definedValue: HeldValue): Vector[DeepLangException] = {
    Vector.empty
  }

  /**
   * Map of fields that should be used in each parameter's Json representation.
   * ParametersSchema containing this parameter will use these fields along with parameters name
   * to describe its contents.
   */
  private[deeplang] def jsDescription: Map[String, JsValue] = {
    Map(
      "type" -> parameterType.toString.toJson,
      "description" -> description.toJson)
  }

  /**
   * Json representation of value held by this parameter.
   * If it is not provided, it returns JsNull.
   */
  def valueToJson: JsValue = maybeValue match {
    case Some(definedValue) => definedValueToJson(definedValue)
    case None => JsNull
  }

  /**
   * Json representation of value held by parameter if this value is provided.
   */
  protected def definedValueToJson(definedValue: HeldValue): JsValue

  /**
   * Fills parameter with value based on json representation of this value.
   */
  def fillValueWithJson(jsValue: JsValue): Unit = {
    value = jsValue match {
      case JsNull => None
      case _ => Some(valueFromDefinedJson(jsValue))
    }
  }

  /**
   * Performs all required side effects of setting value.
   * Assumes that `jsValue` is not JsNull.
   * @return Value of parameter based on json representation of this value.
   */
  protected def valueFromDefinedJson(jsValue: JsValue): HeldValue
}
