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

import io.deepsense.deeplang.params.ParameterType
import spray.json._

import scala.reflect.runtime.universe._

class ChoiceParam[T <: Choice](
    val name: String,
    val description: String)
    (implicit tag: TypeTag[T])
  extends AbstractChoiceParam[T, T] {

  override protected def serializeDefault(choice: T): JsValue = JsString(choice.name)

  val parameterType = ParameterType.Choice

  override def valueToJson(value: T): JsValue = choiceToJson(value)

  protected override def valueFromJsMap(jsMap: Map[String, JsValue]): T = {
    if (jsMap.size != 1) {
      throw new DeserializationException(s"There should be only one selected option in choice" +
        s" parameter, but there are ${jsMap.size} in ${jsMap.toString}.")
    }
    val (label, innerJsValue) = jsMap.iterator.next()
    choiceFromJson(label, innerJsValue)
  }
}

object ChoiceParam {
  def apply[T <: Choice : TypeTag](
      name: String,
      description: String): ChoiceParam[T] =
    new ChoiceParam[T](name, description)
}
