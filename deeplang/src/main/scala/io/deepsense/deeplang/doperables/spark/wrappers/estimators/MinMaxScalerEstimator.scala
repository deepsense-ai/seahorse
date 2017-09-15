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

package io.deepsense.deeplang.doperables.spark.wrappers.estimators

import org.apache.spark.ml.feature.{MinMaxScaler => SparkMinMaxScaler, MinMaxScalerModel => SparkMinMaxScalerModel}

import io.deepsense.deeplang.doperables.SparkEstimatorWrapper
import io.deepsense.deeplang.doperables.spark.wrappers.models.MinMaxScalerModel
import io.deepsense.deeplang.doperables.spark.wrappers.params.common.{HasInputColumn, HasOutputColumn, MinMaxParams}
import io.deepsense.deeplang.params.Param

class MinMaxScalerEstimator
  extends SparkEstimatorWrapper[SparkMinMaxScalerModel, SparkMinMaxScaler, MinMaxScalerModel]
  with MinMaxParams
  with HasInputColumn
  with HasOutputColumn {

  override val params: Array[Param[_]] = declareParams(min, max, inputColumn, outputColumn)
}
