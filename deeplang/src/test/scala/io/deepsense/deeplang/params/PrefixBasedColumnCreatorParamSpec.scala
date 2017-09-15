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

class PrefixBasedColumnCreatorParamSpec
  extends AbstractParamSpec[String, PrefixBasedColumnCreatorParam] {

  override def className: String = "PrefixBasedColumnCreatorParam"

  override def paramFixture: (PrefixBasedColumnCreatorParam, JsValue) = {
    val param = PrefixBasedColumnCreatorParam(
      name = "Prefix based column creator name",
      description = "Prefix based column creator description"
    )
    val expectedJson = JsObject(
      "type" -> JsString("prefixBasedCreator"),
      "name" -> JsString(param.name),
      "description" -> JsString(param.description),
      "isGriddable" -> JsFalse,
      "default" -> JsNull
    )
    (param, expectedJson)
  }

  override def valueFixture: (String, JsValue) = {
    val value = "abc"
    (value, JsString(value))
  }
}
