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

class WorkflowParamSpec extends AbstractParamSpec[JsObject, WorkflowParam] {

  override def className: String = "WorkflowParam"

  override def paramFixture: (WorkflowParam, JsValue) = {
    val description = "Workflow parameter description"
    val param = WorkflowParam(
      name = "Workflow parameter name",
      description = Some(description))
    val expectedJson = JsObject(
      "type" -> JsString("workflow"),
      "name" -> JsString(param.name),
      "description" -> JsString(description),
      "isGriddable" -> JsFalse,
      "default" -> JsNull
    )
    (param, expectedJson)
  }

  override def valueFixture: (JsObject, JsValue) = {
    val value = JsObject(
      "field" -> JsString("value"),
      "array" -> JsArray(
        JsString("one"),
        JsString("two"),
        JsString("three")
      ),
      "object" -> JsObject(
        "inner" -> JsString("value")
      )
    )
    (value, value.copy())
  }
}
