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

import org.apache.spark.ml.regression.{RandomForestRegressor => SparkRFR}

import io.deepsense.deeplang.params.ParamPair
import io.deepsense.deeplang.params.selections.NameSingleColumnSelection

class RandomForestRegressionSmokeTest extends AbstractEstimatorModelWrapperSmokeTest[SparkRFR] {

  override def className: String = "RandomForestRegression"

  override val estimatorWrapper = new RandomForestRegression()

  import estimatorWrapper._

  override val estimatorParams: Seq[ParamPair[_]] = Seq(
    maxDepth -> 5.0,
    maxBins -> 32.0,
    minInstancesPerNode -> 1.0,
    minInfoGain -> 0.0,
    maxMemoryInMB -> 256.0,
    cacheNodeIds -> false,
    checkpointInterval -> 10.0,
    impurity -> Impurity.Variance(),
    subsamplingRate -> 1.0,
    seed -> 1.0,
    numTrees -> 20.0,
    featureSubsetStrategy -> FeatureSubsetStrategy.Auto(),
    featuresColumn -> NameSingleColumnSelection("myFeatures"),
    labelColumn -> NameSingleColumnSelection("myLabel")
  )
}
