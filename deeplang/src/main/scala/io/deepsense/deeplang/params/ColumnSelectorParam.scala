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

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.deeplang.parameters.{MultipleColumnSelectionProtocol, ParameterType, MultipleColumnSelection}
import MultipleColumnSelectionProtocol._

case class ColumnSelectorParam(
    name: String,
    description: String,
    portIndex: Int,
    override val index: Int = 0)
  extends AbstractColumnSelectorParam[MultipleColumnSelection] {

  override val parameterType = ParameterType.ColumnSelector
  override val isSingle = false
}
