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

import ai.deepsense.deeplang.params.selections.{ColumnSelection, MultipleColumnSelection, NameColumnSelection}

class ColumnSelectorParamSpec
  extends AbstractParamSpec[MultipleColumnSelection, ColumnSelectorParam] {

  override def className: String = "MultipleColumnCreatorParam"

  override def paramFixture: (ColumnSelectorParam, JsValue) = {
    val description = "Column selector description"
    val param = ColumnSelectorParam(
      name = "Column selector name",
      description = Some(description),
      portIndex = 0)
    val expectedJson = JsObject(
      "type" -> JsString("selector"),
      "name" -> JsString(param.name),
      "description" -> JsString(description),
      "portIndex" -> JsNumber(param.portIndex),
      "isSingle" -> JsFalse,
      "isGriddable" -> JsFalse,
      "default" -> JsNull
    )
    (param, expectedJson)
  }

  override def valueFixture: (MultipleColumnSelection, JsValue) = {
    val value = MultipleColumnSelection(
      selections = Vector[ColumnSelection](
        NameColumnSelection(Set("a", "b"))
      ),
      excluding = false)
    val expectedJson = JsObject(
      "selections" -> JsArray(
        JsObject(
          "type" -> JsString("columnList"),
          "values" -> JsArray(JsString("a"), JsString("b"))
        )
      ),
      "excluding" -> JsFalse
    )
    (value, expectedJson)
  }
}
