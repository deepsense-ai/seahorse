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

import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.RegressionImpurity.Variance
import ai.deepsense.deeplang.params.ParamPair
import ai.deepsense.deeplang.params.selections.NameSingleColumnSelection

class GBTRegressionSmokeTest extends AbstractEstimatorModelWrapperSmokeTest {

  override def className: String = "GBTRegression"

  override val estimator = new GBTRegression()

  private val labelColumnName = "myRating"

  import estimator._

  override val estimatorParams: Seq[ParamPair[_]] = Seq(
    featuresColumn -> NameSingleColumnSelection("myFeatures"),
    impurity -> Variance(),
    labelColumn -> NameSingleColumnSelection(labelColumnName),
    lossType -> GBTRegression.Squared(),
    maxBins -> 2.0,
    maxDepth -> 6.0,
    maxIterations -> 10.0,
    minInfoGain -> 0.0,
    minInstancesPerNode -> 1,
    predictionColumn -> "prediction",
    seed -> 100.0,
    stepSize -> 0.11,
    subsamplingRate -> 0.999
  )
}
