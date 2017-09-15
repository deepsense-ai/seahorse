/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.params.choice

import scala.reflect.runtime.universe._

import spray.json._

class MultipleChoiceParamSpec
  extends AbstractChoiceParamSpec[Set[ChoiceABC], MultipleChoiceParam[ChoiceABC]] {

  override def className: String = "MultipleChoiceParam"

  className should {
    "serialize default values properly" in {
      val allChoicesSelection: Set[ChoiceABC] = Set(OptionA(), OptionB(), OptionC())
      val expected = JsArray(JsString("A"), JsString("B"), JsString("C"))
      val serializedArray = serializeDefaultValue(allChoicesSelection).asInstanceOf[JsArray]
      serializedArray.elements should contain theSameElementsAs expected.elements
    }
  }

  override def paramFixture: (MultipleChoiceParam[ChoiceABC], JsValue) = {
    val description = "description"
    val multipleChoiceParam = MultipleChoiceParam[ChoiceABC]("name", Some(description))
    val multipleChoiceExpectedJson = JsObject(
      "type" -> JsString("multipleChoice"),
      "name" -> JsString(multipleChoiceParam.name),
      "description" -> JsString(description),
      "isGriddable" -> JsFalse,
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

  override def serializeDefaultValue(default: Set[ChoiceABC]): JsValue =
    JsArray(default.toSeq.map(_.name).map(JsString(_)): _*)

  override protected def createChoiceParam[V <: Choice : TypeTag](
      name: String,
      description: String): ChoiceParam[V] =
    ChoiceParam[V](name, Some(description))
}
