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

import org.apache.spark.ml.regression.{RandomForestRegressionModel => SparkRFRModel, RandomForestRegressor => SparkRFR}

import ai.deepsense.commons.utils.Logging
import ai.deepsense.deeplang.doperables.SparkEstimatorWrapper
import ai.deepsense.deeplang.doperables.spark.wrappers.models.RandomForestRegressionModel
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common._
import ai.deepsense.deeplang.params.Param
import ai.deepsense.deeplang.params.choice.Choice
import ai.deepsense.deeplang.params.validators.RangeValidator
import ai.deepsense.deeplang.params.wrappers.spark._

class RandomForestRegression
  extends SparkEstimatorWrapper[SparkRFRModel, SparkRFR, RandomForestRegressionModel]
  with PredictorParams
  with HasLabelColumnParam
  with HasSeedParam
  with HasMaxDepthParam
  with HasMinInstancePerNodeParam
  with HasMaxBinsParam
  with HasSubsamplingRateParam
  with HasMinInfoGainParam
  with HasMaxMemoryInMBParam
  with HasCacheNodeIdsParam
  with HasCheckpointIntervalParam
  with HasNumTreesParam
  with HasFeatureSubsetStrategyParam
  with HasRegressionImpurityParam
  with Logging {

  override val params: Array[Param[_]] = Array(
    maxDepth,
    maxBins,
    minInstancesPerNode,
    minInfoGain,
    maxMemoryInMB,
    cacheNodeIds,
    checkpointInterval,
    impurity,
    subsamplingRate,
    seed,
    numTrees,
    featureSubsetStrategy,
    labelColumn,
    featuresColumn,
    predictionColumn)
}
