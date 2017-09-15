/**
 * Copyright 2016, deepsense.io
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

import spray.json._

import io.deepsense.deeplang.params.validators.RangeValidator

class MultipleNumericParamSpec extends AbstractParamSpec[Array[Double], MultipleNumericParam] {

  override def className: String = "NumericParam"

  className should {
    "validate its values" when {
      val (param, _) = paramFixture
      "value set is empty" in {
        param.validate(Array()) shouldBe empty
      }
      "values are correct" in {
        param.validate(Array(1.0, 2.0, 2.5)) shouldBe empty
      }
      "values are incorrect" in {
        param.validate(Array(1.0, 100.0, 200.0)) should have size 2
      }
    }
  }

  override def paramFixture: (MultipleNumericParam, JsValue) = {
    val param = MultipleNumericParam(
      name = "Multiple numeric parameter",
      description = "Multiple numeric parameter description",
      validator = RangeValidator(1.0, 3.0, true, false))
    val json = JsObject(
      "type" -> JsString("multipleNumeric"),
      "name" -> JsString(param.name),
      "description" -> JsString(param.description + param.constraints),
      "default" -> JsNull,
      "isGriddable" -> JsFalse,
      "validator" -> JsObject(
        "type" -> JsString("range"),
        "configuration" -> JsObject(
          "begin" -> JsNumber(1.0),
          "end" -> JsNumber(3.0),
          "beginIncluded" -> JsBoolean(true),
          "endIncluded" -> JsBoolean(false)
        )
      )
    )
    (param, json)
  }

  override def valueFixture: (Array[Double], JsValue) = {
    val value = Array(1.0, 2.0, 3.0)
    val jsonValue = JsObject(
      "values" -> JsArray(
        JsObject(
          "type" -> JsString("seq"),
          "value" -> JsObject(
            "sequence" -> JsArray(
              JsNumber(1.0),
              JsNumber(2.0),
              JsNumber(3.0)
            )
          )
        )
      )
    )
    (value, jsonValue)
  }
}
