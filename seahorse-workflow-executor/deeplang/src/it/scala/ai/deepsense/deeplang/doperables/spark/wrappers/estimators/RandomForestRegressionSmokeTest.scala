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

import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.FeatureSubsetStrategy
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.RegressionImpurity.Variance
import ai.deepsense.deeplang.params.ParamPair
import ai.deepsense.deeplang.params.selections.NameSingleColumnSelection

class RandomForestRegressionSmokeTest extends AbstractEstimatorModelWrapperSmokeTest {

  override def className: String = "RandomForestRegression"

  override val estimator = new RandomForestRegression()

  import estimator._

  override val estimatorParams: Seq[ParamPair[_]] = Seq(
    maxDepth -> 5.0,
    maxBins -> 32.0,
    minInstancesPerNode -> 1.0,
    minInfoGain -> 0.0,
    maxMemoryInMB -> 256.0,
    cacheNodeIds -> false,
    checkpointInterval -> 10.0,
    impurity -> Variance(),
    subsamplingRate -> 1.0,
    seed -> 1.0,
    numTrees -> 20.0,
    featureSubsetStrategy -> FeatureSubsetStrategy.Auto(),
    featuresColumn -> NameSingleColumnSelection("myFeatures"),
    labelColumn -> NameSingleColumnSelection("myLabel")
  )
}
