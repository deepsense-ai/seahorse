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

package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.regression.{GeneralizedLinearAlgorithm, LassoModel, LassoWithSGD}
import org.scalactic.EqualityPolicy.Spread

import io.deepsense.deeplang.doperables.machinelearning.LinearRegressionParameters
import io.deepsense.deeplang.doperables.machinelearning.lassoregression.{TrainedLassoRegression, UntrainedLassoRegression}

class UntrainedLassoRegressionIntegSpec extends UntrainedRegressionIntegSpec[LassoModel] {

  val testDataDir: String = testsDir + "/UntrainedLassoRegressionIntegSpec"

  override def regressionName: String = "UntrainedLassoRegression"

  override def modelType: Class[LassoModel] = classOf[LassoModel]

  override def constructUntrainedModel(
      untrainedModelMock: GeneralizedLinearAlgorithm[LassoModel]): Trainable =
    UntrainedLassoRegression(
      () => untrainedModelMock.asInstanceOf[LassoWithSGD],
      mock[LinearRegressionParameters])

  override def mockUntrainedModel(): GeneralizedLinearAlgorithm[LassoModel] =
    mock[LassoWithSGD]

  override val featuresValues: Seq[Spread[Double]] = Seq(
    Spread(0.0, 0.0),
    -0.755 +- 0.01,
    Spread(0.0, 0.0),
    -0.377 +- 0.01,
    Spread(0.0, 0.0),
    1.133 +- 0.01
  )

  override def validateResult(
      mockTrainedModel: LassoModel,
      result: Scorable,
      targetColumnName: String): Registration = {

    val castedResult = result.asInstanceOf[TrainedLassoRegression]
    castedResult.model shouldBe mockTrainedModel
    castedResult.featureColumns shouldBe Seq("column1", "column0")
    castedResult.targetColumn shouldBe targetColumnName
  }
}
