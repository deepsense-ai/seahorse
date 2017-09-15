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

package io.deepsense.deeplang.params.choice

import scala.reflect.runtime.universe._

import spray.json.{JsObject, JsValue}

import io.deepsense.deeplang.parameters.ParameterType

case class MultipleChoiceParam[T <: Choice](
    name: String,
    description: String)
  (implicit tag: TypeTag[T])
  extends AbstractChoiceParam[T, Set[T]] {

  val parameterType = ParameterType.MultipleChoice

  override def valueToJson(value: Set[T]): JsValue =
    value.foldLeft(JsObject())(
      (acc: JsObject, choice: T) => JsObject(acc.fields ++ choiceToJson(choice).fields))

  protected override def valueFromJsMap(jsMap: Map[String, JsValue]): Set[T] = {
    jsMap.toList.map {
      case (label, innerJsValue) => choiceFromJson(label, innerJsValue)
    }.toSet
  }
}
