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

package io.deepsense.deeplang.params

import java.util.Objects

import spray.json.DefaultJsonProtocol._
import spray.json._

import io.deepsense.deeplang.exceptions.DeepLangException
import io.deepsense.deeplang.params.ParameterType._

abstract class Param[T] {

  val name: String

  val description: String

  val constraints: String = ""

  val parameterType: ParameterType

  def validate(value: T): Vector[DeepLangException] = Vector.empty

  def validateSubparams(value: T): Vector[DeepLangException] = Vector.empty

  val isGriddable: Boolean = false

  /**
   * Describes json representation of this parameter.
   * @param maybeDefault Optional default value of parameter. Should be of type Option[T],
   *                     but we need to receive Any because Params have to use this method
   *                     without knowing T.
   */
  def toJson(maybeDefault: Option[Any]): JsObject = {
    val basicFields = Map(
      "name" -> name.toJson,
      "type" -> parameterType.toString.toJson, // TODO json format for parameterType
      "description" -> (description + constraints).toJson,
      "isGriddable" -> isGriddable.toJson,
      "default" -> maybeDefault.map(default =>
        serializeDefault(default.asInstanceOf[T])).getOrElse(JsNull)
    )
    JsObject(basicFields ++ extraJsFields)
  }

  /**
    * Describes default serialization of default values.
    * @param default Default value of parameter
    */
  protected def serializeDefault(default: T): JsValue = valueToJson(default)

  /**
   * Subclasses should overwrite this method if they want to
   * add custom fields to json description.
   */
  protected def extraJsFields: Map[String, JsValue] = Map.empty

  def ->(value: T): ParamPair[T] = ParamPair(this, value)

  override def toString: String = s"Param($parameterType, $name)"

  def valueToJson(value: T): JsValue

  /**
   * Helper method for Params, which don't know T.
   */
  private[params] def anyValueToJson(value: Any): JsValue = valueToJson(value.asInstanceOf[T])

  def valueFromJson(jsValue: JsValue): T


  def canEqual(other: Any): Boolean = other.isInstanceOf[Param[T]]

  override def equals(other: Any): Boolean = other match {
    case that: Param[T] =>
      (that canEqual this) &&
        name == that.name &&
        description == that.description &&
        parameterType == that.parameterType
    case _ => false
  }

  override def hashCode(): Int = {
    Objects.hash(name, description, parameterType)
  }
}
