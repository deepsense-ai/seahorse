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

import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.OptionalQuantilesColumnChoice
import ai.deepsense.deeplang.params.ParamPair
import ai.deepsense.deeplang.params.selections.NameSingleColumnSelection

class AFTSurvivalRegressionSmokeTest
  extends AbstractEstimatorModelWrapperSmokeTest {

  override def className: String = "AFTSurvivalRegression"

  override val estimator = new AFTSurvivalRegression()

  import estimator._

  val optionalQuantilesChoice = OptionalQuantilesColumnChoice.QuantilesColumnNoOption()

  override val estimatorParams: Seq[ParamPair[_]] = Seq(
    censorColumn -> NameSingleColumnSelection("myCensor"),
    fitIntercept -> true,
    maxIterations -> 2.0,
    tolerance -> 0.01,
    featuresColumn -> NameSingleColumnSelection("myStandardizedFeatures"),
    labelColumn -> NameSingleColumnSelection("myNoZeroLabel"),
    predictionColumn -> "pred",
    optionalQuantilesColumn -> optionalQuantilesChoice,
    quantileProbabilities -> Array(0.01, 0.05, 0.1, 0.25, 0.5, 0.75, 0.9, 0.95, 0.99)
  )
}
