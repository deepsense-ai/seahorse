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

class BooleanParamSpec extends AbstractParamSpec[Boolean, BooleanParam] {

  override def className: String = "BooleanParam"

  override def paramFixture: (BooleanParam, JsValue) = {
    val param = BooleanParam(
      name = "Boolean param name",
      description = "Boolean param description")
    val json = JsObject(
      "type" -> JsString("boolean"),
      "name" -> JsString(param.name),
      "description" -> JsString(param.description),
      "isGriddable" -> JsFalse,
      "default" -> JsNull
    )
    (param, json)
  }

  override def valueFixture: (Boolean, JsValue) = (true, JsBoolean(true))
}
