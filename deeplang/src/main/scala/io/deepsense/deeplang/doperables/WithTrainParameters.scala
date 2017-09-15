/**
 * Copyright 2015, CodiLime Inc.
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

package io.deepsense.deeplang.doperables

import io.deepsense.deeplang.parameters.{ColumnSelectorParameter, ParametersSchema, SingleColumnSelectorParameter}

trait WithTrainParameters {
  private val featureColumnsField = "feature columns"

  private val targetColumnField = "target column"

  protected val dataFramePortIndex: Int

  protected val trainParameters = ParametersSchema(
    featureColumnsField -> ColumnSelectorParameter(
      "Columns which are to be used as features in regression",
      required = true,
      portIndex = dataFramePortIndex),
    targetColumnField -> SingleColumnSelectorParameter(
      "Column against which the regression will be performed",
      required = true,
      portIndex = dataFramePortIndex))

  protected def parametersForTrainable: Trainable.Parameters = Trainable.Parameters(
    trainParameters.getColumnSelection(featureColumnsField),
    trainParameters.getSingleColumnSelection(targetColumnField))
}
