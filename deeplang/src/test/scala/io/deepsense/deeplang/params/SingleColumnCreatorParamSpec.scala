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

import spray.json._

import io.deepsense.deeplang.params.validators.ColumnNameValidator

class SingleColumnCreatorParamSpec extends AbstractParamSpec[String, SingleColumnCreatorParam] {

  override def className: String = "SingleColumnCreatorParam"

  override def paramFixture: (SingleColumnCreatorParam, JsValue) = {
    val param = SingleColumnCreatorParam(
      name = "Single column creator name",
      description = "Single column creator description")
    val expectedJson = JsObject(
      "type" -> JsString("creator"),
      "name" -> JsString(param.name),
      "description" -> JsString(param.description),
      "isGriddable" -> JsFalse,
      "default" -> JsNull,
      "validator" -> JsObject(
        "type" -> JsString("regex"),
        "configuration" -> JsObject(
          "regex" -> JsString(ColumnNameValidator.regex.toString())
        )
      )
    )
    (param, expectedJson)
  }

  override def valueFixture: (String, JsValue) = {
    val value = "abc"
    (value, JsString(value))
  }
}
