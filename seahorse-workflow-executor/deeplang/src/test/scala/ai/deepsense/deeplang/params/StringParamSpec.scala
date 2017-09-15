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

import ai.deepsense.deeplang.params.validators.AcceptAllRegexValidator

class StringParamSpec extends AbstractParamSpec[String, StringParam] {

  override def className: String = "StringParam"

  override def paramFixture: (StringParam, JsValue) = {
    val description = "String parameter description"
    val param = StringParam(
      name = "String parameter name",
      description = Some(description),
      validator = new AcceptAllRegexValidator)
    val expectedJson = JsObject(
      "type" -> JsString("string"),
      "name" -> JsString(param.name),
      "description" -> JsString(description),
      "default" -> JsNull,
      "isGriddable" -> JsFalse,
      "validator" -> JsObject(
        "type" -> JsString("regex"),
        "configuration" -> JsObject(
          "regex" -> JsString(".*")
        )
      )
    )
    (param, expectedJson)
  }

  override def valueFixture: (String, JsValue) = {
    val value = "abcdefghij"
    (value, JsString(value))
  }
}
