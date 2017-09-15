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

package ai.deepsense.deeplang.params.datasource

import java.util.UUID

import spray.json._
import ai.deepsense.deeplang.params.AbstractParamSpec

class DatasourceIdForReadParamSpec extends AbstractParamSpec[UUID, DatasourceIdForReadParam] {

  override def className: String = "DatasourceIdForReadParam"

  override def paramFixture: (DatasourceIdForReadParam, JsValue) = {
    val param = DatasourceIdForReadParam(
      name = "Ds for read parameter name",
      description = None)
    val expectedJson = JsObject(
      "type" -> JsString("datasourceIdForRead"),
      "name" -> JsString(param.name),
      "description" -> JsString(""),
      "default" -> JsNull,
      "isGriddable" -> JsFalse
    )
    (param, expectedJson)
  }

  override def valueFixture: (UUID, JsValue) = {
    val value = UUID.randomUUID()
    (value, JsString(value.toString))
  }
}
