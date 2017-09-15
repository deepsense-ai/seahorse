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

import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.{SolverChoice, OptionalWeightColumnChoice}
import ai.deepsense.deeplang.params.ParamPair
import ai.deepsense.deeplang.params.selections.NameSingleColumnSelection

class LinearRegressionSmokeTest
  extends AbstractEstimatorModelWrapperSmokeTest {

  override def className: String = "LinearRegression"

  override val estimator = new LinearRegression()

  import estimator._

  val weightColumnChoice = OptionalWeightColumnChoice.WeightColumnYesOption()
    .setWeightColumn(NameSingleColumnSelection("myWeight"))

  override val estimatorParams: Seq[ParamPair[_]] = Seq(
    elasticNetParam -> 0.8,
    fitIntercept -> true,
    maxIterations -> 2.0,
    regularizationParam -> 0.1,
    tolerance -> 0.01,
    standardization -> true,
    featuresColumn -> NameSingleColumnSelection("myFeatures"),
    labelColumn -> NameSingleColumnSelection("myLabel"),
    predictionColumn -> "pred",
    optionalWeightColumn -> weightColumnChoice,
    solver -> SolverChoice.Auto()
  )
}
