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

package io.deepsense.deeplang.doperations

import spray.json.JsValue

import io.deepsense.deeplang.exceptions.DeepLangException
import io.deepsense.deeplang.parameters.ParametersSchema
import io.deepsense.deeplang.params.ParameterJsonContainer

trait OldOperation extends ParameterJsonContainer {

  val parameters: ParametersSchema

  def paramsToJson: JsValue = parameters.paramsToJson

  def paramValuesToJson: JsValue = parameters.paramValuesToJson

  def setParamsFromJson(jsValue: JsValue): this.type = {
    parameters.setParamsFromJson(jsValue)
    this
  }

  def validateParams(): Vector[DeepLangException] = parameters.validateParams
}
