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

import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.OptionalWeightColumnChoice
import ai.deepsense.deeplang.params.ParamPair
import ai.deepsense.deeplang.params.selections.NameSingleColumnSelection

class IsotonicRegressionWithWeightsSmokeTest
  extends AbstractEstimatorModelWrapperSmokeTest {

  override def className: String = "IsotonicRegression"

  override val estimator = new IsotonicRegression()

  import estimator._

  val weightColumnName = "myWeight"

  val weightColumnChoice = OptionalWeightColumnChoice.WeightColumnYesOption()
    .setWeightColumn(NameSingleColumnSelection(weightColumnName))

  override val estimatorParams: Seq[ParamPair[_]] = Seq(
    featureIndex -> 1,
    featuresColumn -> NameSingleColumnSelection("myFeatures"),
    isotonic -> true,
    labelColumn -> NameSingleColumnSelection("myLabel"),
    predictionColumn -> "isotonicPrediction",
    optionalWeightColumn -> weightColumnChoice)

  className should {
    "pass weight column value to wrapped model" in {
      val estimatorWithParams = estimator.set(estimatorParams: _*)
      val sparkEstimator = estimatorWithParams.sparkEstimator
      val sparkParamMap = estimatorWithParams.sparkParamMap(
        sparkEstimator,
        dataFrame.sparkDataFrame.schema)
      sparkParamMap.get(
        estimator.sparkEstimator.weightCol) shouldBe Some(weightColumnName)
    }
  }
}
