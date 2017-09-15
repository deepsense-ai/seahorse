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

import ai.deepsense.deeplang.params.validators.RangeValidator

class NumericParamSpec extends AbstractParamSpec[Double, NumericParam] {

  override def className: String = "NumericParam"

  override def paramFixture: (NumericParam, JsValue) = {
    val description = "Numeric parameter description"
    val param = NumericParam(
      name = "Numeric parameter",
      description = Some(description),
      validator = RangeValidator(1.0, 3.0, true, false))
    val json = JsObject(
      "type" -> JsString("numeric"),
      "name" -> JsString(param.name),
      "description" -> JsString(
        description + param.constraints),
      "default" -> JsNull,
      "isGriddable" -> JsTrue,
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

  override def valueFixture: (Double, JsValue) = {
    val value = 2.5
    (value, JsNumber(value))
  }
}
