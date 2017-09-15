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

import spray.json._

class MultipleChoiceParamSpec
  extends AbstractChoiceParamSpec[Set[ChoiceABC], MultipleChoiceParam[ChoiceABC]] {

  override def className: String = "MultipleChoiceParam"

  override def paramFixture: (MultipleChoiceParam[ChoiceABC], JsValue) = {
    val multipleChoiceParam = MultipleChoiceParam[ChoiceABC]("name", "description")
    val multipleChoiceExpectedJson = JsObject(
      "type" -> JsString("multipleChoice"),
      "name" -> JsString(multipleChoiceParam.name),
      "description" -> JsString(multipleChoiceParam.description),
      "default" -> JsNull,
      ChoiceFixtures.values)
    (multipleChoiceParam, multipleChoiceExpectedJson)
  }

  override def valueFixture: (Set[ChoiceABC], JsValue) = {
    val choices = Set[ChoiceABC](
      OptionA().setBool(true),
      OptionC()
    )
    val expectedJson = JsObject(
      "A" -> JsObject(
        "bool" -> JsTrue
      ),
      "C" -> JsObject()
    )
    (choices, expectedJson)
  }

  override protected def createChoiceParam[V <: Choice : TypeTag](
      name: String,
      description: String): ChoiceParam[V] =
    ChoiceParam[V](name, description)
}
