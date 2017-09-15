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

import org.apache.spark.ml.feature.{StandardScaler => SparkStandardScaler, StandardScalerModel => SparkStandardScalerModel}

import ai.deepsense.deeplang.doperables.SparkSingleColumnEstimatorWrapper
import ai.deepsense.deeplang.doperables.spark.wrappers.models.StandardScalerModel
import ai.deepsense.deeplang.params.Param
import ai.deepsense.deeplang.params.wrappers.spark.BooleanParamWrapper

class StandardScalerEstimator
  extends SparkSingleColumnEstimatorWrapper[
    SparkStandardScalerModel,
    SparkStandardScaler,
    StandardScalerModel] {

  override def convertInputNumericToVector: Boolean = true
  override def convertOutputVectorToDouble: Boolean = true

  val withMean = new BooleanParamWrapper[SparkStandardScaler](
    name = "with mean",
    description = Some("Whether to center data with mean."),
    sparkParamGetter = _.withMean)
  setDefault(withMean, false)

  val withStd = new BooleanParamWrapper[SparkStandardScaler](
    name = "with std",
    description = Some("Whether to scale the data to unit standard deviation."),
    sparkParamGetter = _.withStd)
  setDefault(withStd, true)

  override protected def getSpecificParams: Array[Param[_]] = Array(withMean, withStd)
}
