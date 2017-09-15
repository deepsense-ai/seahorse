/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.RegressionImpurity.Variance
import ai.deepsense.deeplang.params.ParamPair
import ai.deepsense.deeplang.params.selections.NameSingleColumnSelection

class DecisionTreeRegressionSmokeTest extends AbstractEstimatorModelWrapperSmokeTest {

  override def className: String = "DecisionTreeRegression"

  override val estimator = new DecisionTreeRegression()

  import estimator._

  override val estimatorParams: Seq[ParamPair[_]] = Seq(
    maxDepth -> 4.0,
    maxBins -> 25.0,
    minInstancesPerNode -> 1.0,
    minInfoGain -> 0.1,
    maxMemoryInMB -> 200.0,
    cacheNodeIds -> false,
    checkpointInterval -> 11.0,
    seed -> 125.0,
    impurity -> Variance(),
    featuresColumn -> NameSingleColumnSelection("myFeatures"),
    labelColumn -> NameSingleColumnSelection("myLabel"),
    predictionColumn -> "pred"
  )
}
