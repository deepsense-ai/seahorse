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

package ai.deepsense.deeplang.doperables.spark.wrappers.estimators

import org.apache.spark.ml.feature.{MinMaxScaler => SparkMinMaxScaler, MinMaxScalerModel => SparkMinMaxScalerModel}

import ai.deepsense.deeplang.doperables.SparkSingleColumnEstimatorWrapper
import ai.deepsense.deeplang.doperables.spark.wrappers.models.MinMaxScalerModel
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.{HasInputColumn, HasOutputColumn, MinMaxParams}
import ai.deepsense.deeplang.params.Param

class MinMaxScalerEstimator
  extends SparkSingleColumnEstimatorWrapper[
    SparkMinMaxScalerModel,
    SparkMinMaxScaler,
    MinMaxScalerModel]
  with MinMaxParams
  with HasInputColumn
  with HasOutputColumn {

  override def convertInputNumericToVector: Boolean = true
  override def convertOutputVectorToDouble: Boolean = true

  override def getSpecificParams: Array[Param[_]] = Array(min, max)
}
