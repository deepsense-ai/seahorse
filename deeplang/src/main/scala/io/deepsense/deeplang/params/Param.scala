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

import spray.json.DefaultJsonProtocol._
import spray.json._

import io.deepsense.deeplang.exceptions.DeepLangException
import io.deepsense.deeplang.parameters.ParameterType._

abstract class Param[T] {

  val name: String

  val description: String

  val parameterType: ParameterType

  def validate(value: T): Vector[DeepLangException] = Vector.empty

  def toJson: JsObject = {
    val basicFields = Map(
      "name" -> name,
      "type" -> parameterType.toString, // TODO json format for parameterType
      "description" -> description
    ).toJson.asJsObject.fields
    JsObject(basicFields ++ extraJsFields)
  }

  /**
   * Subclasses should overwrite this method if they want to
   * add custom fields to json description.
   */
  protected def extraJsFields: Map[String, JsValue] = Map.empty

  def ->(value: T): ParamPair[T] = ParamPair(this, value)

  override def toString: String = s"Param($parameterType, $name)"

  def valueToJson(value: T): JsValue

  def anyValueToJson(value: Any): JsValue = valueToJson(value.asInstanceOf[T])

  def valueFromJson(jsValue: JsValue): T
}
