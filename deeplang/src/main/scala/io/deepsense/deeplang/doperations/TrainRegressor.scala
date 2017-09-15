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

import scala.reflect.runtime.{universe => ru}

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.{Regressor, Scorable, Trainable}
import io.deepsense.deeplang.parameters.{ColumnSelectorParameter, MultipleColumnSelection, NameColumnSelection, NameSingleColumnSelection}

case class TrainRegressor() extends Trainer[Regressor with Trainable, Regressor with Scorable] {
  override val id: DOperation.Id = "c526714c-e7fb-11e4-b02c-1681e6b88ec1"
  override val name = "Train Regressor"
  @transient
  override lazy val tTagTI_0: ru.TypeTag[Regressor with Trainable] =
    ru.typeTag[Regressor with Trainable]
  @transient
  override lazy val tTagTO_0: ru.TypeTag[Regressor with Scorable] =
    ru.typeTag[Regressor with Scorable]
  @transient
  override lazy val tTagTI_1: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}

object TrainRegressor {
  def apply(featureColumns: Set[String], targetColumn: String): TrainRegressor = {
    val regressor = TrainRegressor()
    regressor.featureColumnsParameter.value =
      MultipleColumnSelection(Vector(NameColumnSelection(featureColumns)), false)
    regressor.targetColumnParameter.value =
      NameSingleColumnSelection(targetColumn)
    regressor
  }
}
