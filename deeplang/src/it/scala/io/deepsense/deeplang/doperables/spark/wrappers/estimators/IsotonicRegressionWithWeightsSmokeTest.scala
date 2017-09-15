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

import org.apache.spark.ml.regression.{IsotonicRegression => SparkIsotonicRegression}

import io.deepsense.deeplang.doperables.spark.wrappers.estimators.IsotonicRegression.WeightColumnYesOption
import io.deepsense.deeplang.params.ParamPair
import io.deepsense.deeplang.params.selections.NameSingleColumnSelection

class IsotonicRegressionWithWeightsSmokeTest
  extends AbstractEstimatorModelWrapperSmokeTest[SparkIsotonicRegression] {

  override def className: String = "IsotonicRegression"

  override val estimatorWrapper = new IsotonicRegression()

  import estimatorWrapper._

  val weightColumnName = "myLabel"

  val weightColumnChoice = WeightColumnYesOption()
    .setWeightColumn(NameSingleColumnSelection(weightColumnName))

  override val estimatorParams: Seq[ParamPair[_]] = Seq(
    featureIndex -> 1,
    featuresColumn -> NameSingleColumnSelection("myFeatures"),
    isotonic -> true,
    labelColumn -> NameSingleColumnSelection("myLabel"),
    predictionColumn -> "isotonicPrediction",
    useCustomWeights -> weightColumnChoice)

  className should {
    "pass weight column value to wrapped model" in {
      val estimatorWithParams = estimatorWrapper.set(estimatorParams: _*)
      val sparkEstimator = estimatorWithParams.sparkEstimator
      val sparkParamMap = estimatorWithParams.sparkParamMap(
        sparkEstimator,
        dataFrame.sparkDataFrame.schema)
      sparkParamMap.get(estimatorWrapper.sparkEstimator.weightCol) shouldBe Some(weightColumnName)
    }
  }
}
