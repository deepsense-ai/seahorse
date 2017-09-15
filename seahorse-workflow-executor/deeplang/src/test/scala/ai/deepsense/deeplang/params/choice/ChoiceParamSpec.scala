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

import spray.json._
import scala.reflect.runtime.universe._

import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader

class ChoiceParamSpec extends AbstractChoiceParamSpec[ChoiceABC, ChoiceParam[ChoiceABC]] {

  override def className: String = "ChoiceParam"

  className should {
    "throw an exception while deserializing multiple choices" in {
      val graphReader = mock[GraphReader]
      val param = paramFixture._1
      val twoChoicesJson = JsObject(
        "B" -> JsObject(),
        "C" -> JsObject()
      )
      an [DeserializationException] should be thrownBy param.valueFromJson(twoChoicesJson, graphReader)
    }
    "serialize default values properly" in {
      val choices = Seq(OptionA(), OptionB(), OptionC())
      val expected = Seq("A", "B", "C").map(JsString(_))
      choices.map(serializeDefaultValue) should contain theSameElementsAs expected
    }
    "validate choice subparams" in {
      val param = paramFixture._1
      val value = OptionA()
      value.validateParams should not be empty
      param.validate(value) shouldBe value.validateParams
    }
  }

  override def paramFixture: (ChoiceParam[ChoiceABC], JsValue) = {
    val singleChoiceParam = ChoiceParam[ChoiceABC]("name", Some("description"))
    val singleChoiceExpectedJson = JsObject(
      "type" -> JsString("choice"),
      "name" -> JsString(singleChoiceParam.name),
      "description" -> JsString("description"),
      "isGriddable" -> JsFalse,
      "default" -> JsNull,
      ChoiceFixtures.values)
    (singleChoiceParam, singleChoiceExpectedJson)
  }

  override def valueFixture: (ChoiceABC, JsValue) = {
    val choice = OptionA().setBool(true)
    val expectedJson = JsObject(
      "A" -> JsObject(
        "bool" -> JsTrue
      )
    )
    (choice, expectedJson)
  }

  override def serializeDefaultValue(default: ChoiceABC): JsValue = JsString(default.name)

  override protected def createChoiceParam[V <: Choice : TypeTag](
      name: String,
      description: String): ChoiceParam[V] =
    ChoiceParam[V](name, Some(description))
}
