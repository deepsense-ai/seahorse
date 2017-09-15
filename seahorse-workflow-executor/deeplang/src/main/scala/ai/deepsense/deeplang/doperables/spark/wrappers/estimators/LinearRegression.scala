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

import org.apache.spark.ml.regression.{LinearRegression => SparkLinearRegression, LinearRegressionModel => SparkLinearRegressionModel}

import ai.deepsense.deeplang.doperables.SparkEstimatorWrapper
import ai.deepsense.deeplang.doperables.spark.wrappers.models.LinearRegressionModel
import ai.deepsense.deeplang.doperables.spark.wrappers.params.LinearRegressionParams
import ai.deepsense.deeplang.params.Param

class LinearRegression
  extends SparkEstimatorWrapper[
    SparkLinearRegressionModel,
    SparkLinearRegression,
    LinearRegressionModel]
  with LinearRegressionParams {

  override val params: Array[Param[_]] = Array(
    elasticNetParam,
    fitIntercept,
    maxIterations,
    regularizationParam,
    tolerance,
    standardization,
    optionalWeightColumn,
    solver,
    labelColumn,
    featuresColumn,
    predictionColumn)
}
