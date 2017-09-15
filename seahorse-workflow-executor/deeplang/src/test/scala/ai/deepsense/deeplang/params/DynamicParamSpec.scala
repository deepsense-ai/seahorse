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

package ai.deepsense.deeplang.params

import spray.json._

import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader

class DynamicParamSpec extends AbstractParamSpec[JsValue, DynamicParam] {

  override def className: String = "DynamicParam"

  override def paramFixture: (DynamicParam, JsValue) = {
    val description = "Dynamic param description"
    val param = new DynamicParam(
      "Dynamic param name",
      Some(description),
      inputPort = 4)
    val json = JsObject(
      "type" -> JsString("dynamic"),
      "name" -> JsString(param.name),
      "description" -> JsString(description),
      "inputPort" -> JsNumber(param.inputPort),
      "isGriddable" -> JsFalse,
      "default" -> JsNull)
    (param, json)
  }

  override def valueFixture: (JsValue, JsValue) = {
    val anyJson = JsObject("a" -> JsNumber(3), "b" -> JsString("c"))
    (anyJson, anyJson)
  }

  it should {
    "skip JsNull values" in {
      val (param, _) = paramFixture
      val input = JsObject("a" -> JsNumber(3), "b" -> JsNull)
      val expected = JsObject("a" -> JsNumber(3))
      val graphReader = mock[GraphReader]
      param.valueFromJson(input, graphReader) shouldBe expected
    }
  }
}
