/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

import ai.deepsense.deeplang.params.validators.{ArrayLengthValidator, ComplexArrayValidator, RangeValidator}

class MultipleNumericParamSpec extends AbstractParamSpec[Array[Double], MultipleNumericParam] {

  override def className: String = "NumericParam"

  className should {
    "validate its values" when {
      val (param, _) = paramFixture
      "empty value set is too short" in {
        param.validate(Array()) should have size 1
      }
      "values are correct" in {
        param.validate(Array(1.0, 2.0, 2.5)) shouldBe empty
      }
      "two values are incorrect" in {
        param.validate(Array(1.0, 100.0, 200.0)) should have size 2
      }
      "array is too long" in {
        param.validate(Array(1.0, 1.0, 1.0, 1.0, 1.0, 1.0)) should have size 1
      }
      "array is too long and all six values are incorrect" in {
        param.validate(Array(4.0, 5.0, 6.0, 7.5, 100.0, -2.0)) should have size 7
      }
    }
  }

  override def paramFixture: (MultipleNumericParam, JsValue) = {
    val description = "Multiple numeric parameter description"
    val param = MultipleNumericParam(
      name = "Multiple numeric parameter",
      description = Some(description),
      validator = ComplexArrayValidator(
        rangeValidator = RangeValidator(1.0, 3.0, beginIncluded = true, endIncluded = false),
        lengthValidator = ArrayLengthValidator(min = 2, max = 4)))
    val json = JsObject(
      "type" -> JsString("multipleNumeric"),
      "name" -> JsString(param.name),
      "description" -> JsString(
        description + param.constraints),
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
