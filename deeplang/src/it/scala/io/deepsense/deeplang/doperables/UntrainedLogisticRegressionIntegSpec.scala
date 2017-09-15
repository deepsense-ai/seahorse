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

import org.apache.spark.mllib.classification.{LogisticRegressionWithLBFGS, LogisticRegressionModel}
import org.apache.spark.mllib.regression.GeneralizedLinearAlgorithm
import org.scalactic.EqualityPolicy.Spread

import io.deepsense.deeplang.doperables.machinelearning.logisticregression.{UntrainedLogisticRegression, TrainedLogisticRegression}

class UntrainedLogisticRegressionIntegSpec
  extends UntrainedRegressionIntegSpec[LogisticRegressionModel] {

  val testDataDir: String = testsDir + "/UntrainedLogisticRegressionIntegSpec"

  override def regressionName: String = "UntrainedLogisticRegression"

  override def modelType: Class[LogisticRegressionModel] = classOf[LogisticRegressionModel]

  override def constructUntrainedModel: Trainable =
    UntrainedLogisticRegression(
      () => mockUntrainedModel.asInstanceOf[LogisticRegressionWithLBFGS])

  override val mockUntrainedModel: GeneralizedLinearAlgorithm[LogisticRegressionModel] =
    mock[LogisticRegressionWithLBFGS]

  override val featuresValues: Seq[Spread[Double]] = Seq(
    Spread(-2.0, 0.0),
    Spread(1000.0, 0.0),
    Spread(-2.0, 0.0),
    Spread(2000.0, 0.0),
    Spread(-2.0, 0.0),
    Spread(6000.0, 0.0))

  override def validateResult(
      mockTrainedModel: LogisticRegressionModel,
      result: Scorable): Registration = {
    val castedResult = result.asInstanceOf[TrainedLogisticRegression]
    castedResult.model shouldBe mockTrainedModel
    castedResult.featureColumns shouldBe Seq("column1", "column0")
    castedResult.targetColumn shouldBe "column3"
  }
}
