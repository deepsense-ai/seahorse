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

import org.apache.spark.ml.classification.{LogisticRegression => SparkLogisticRegression, LogisticRegressionModel => SparkLogisticRegressionModel}

import ai.deepsense.deeplang.doperables.SparkEstimatorWrapper
import ai.deepsense.deeplang.doperables.spark.wrappers.models.LogisticRegressionModel
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common._
import ai.deepsense.deeplang.params.Param

class LogisticRegression
  extends SparkEstimatorWrapper[
    SparkLogisticRegressionModel,
    SparkLogisticRegression,
    LogisticRegressionModel]
  with ProbabilisticClassifierParams
  with HasLabelColumnParam
  with HasThreshold
  with HasRegularizationParam
  with HasElasticNetParam
  with HasMaxIterationsParam
  with HasTolerance
  with HasFitIntercept
  with HasStandardization
  with HasOptionalWeightColumnParam {

  override lazy val maxIterationsDefault = 100.0

  override val params: Array[Param[_]] = Array(
    elasticNetParam,
    fitIntercept,
    maxIterations,
    regularizationParam,
    tolerance,
    standardization,
    optionalWeightColumn,
    labelColumn,
    featuresColumn,
    probabilityColumn,
    rawPredictionColumn,
    predictionColumn,
    threshold)
}
