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

import org.apache.spark.mllib.classification.{LogisticRegressionModel, LogisticRegressionWithLBFGS}
import org.apache.spark.mllib.regression.GeneralizedLinearAlgorithm
import org.scalactic.EqualityPolicy.Spread

import io.deepsense.deeplang.doperables.machinelearning.logisticregression.{TrainedLogisticRegression, UntrainedLogisticRegression}
import io.deepsense.deeplang.doperations.exceptions.WrongColumnTypeException
import io.deepsense.deeplang.parameters.{MultipleColumnSelection, NameColumnSelection, NameSingleColumnSelection}

class UntrainedLogisticRegressionIntegSpec
  extends UntrainedRegressionIntegSpec[LogisticRegressionModel] {

  val testDataDir: String = testsDir + "/UntrainedLogisticRegressionIntegSpec"

  override def regressionName: String = "UntrainedLogisticRegression"

  override def modelType: Class[LogisticRegressionModel] = classOf[LogisticRegressionModel]

  override def constructUntrainedModel(
      untrainedModelMock: GeneralizedLinearAlgorithm[LogisticRegressionModel]): Trainable =
    UntrainedLogisticRegression(
      () => untrainedModelMock.asInstanceOf[LogisticRegressionWithLBFGS])

  override def mockUntrainedModel(): GeneralizedLinearAlgorithm[LogisticRegressionModel] =
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
      result: Scorable,
      targetColumnName: String): Registration = {
    val castResult = result.asInstanceOf[TrainedLogisticRegression]
    castResult.model shouldBe mockTrainedModel
    castResult.featureColumns shouldBe Seq("column1", "column0")
    castResult.targetColumn shouldBe targetColumnName
  }

  regressionName should {

    "train a model with 2-level categorical as target" in
      withMockedModel(Seq(0.0, 1.0, 0.0)) {
        (untrainedModel, trainedModel, context) =>
          val dataFrame = createDataFrame(inputRows, inputSchema, Seq("column5"))

          val parameters = Trainable.Parameters(
            featureColumns = Some(MultipleColumnSelection(
              Vector(NameColumnSelection(Set("column1", "column0"))))),
            targetColumn = Some(NameSingleColumnSelection("column5")))

          val result = untrainedModel.train(context)(parameters)(dataFrame)
          validateResult(trainedModel, result, "column5")
      }

    "train a model with boolean as target" in
      withMockedModel(Seq(1.0, 1.0, 0.0)) {
        (untrainedModel, trainedModel, context) =>
          val dataFrame = createDataFrame(inputRows, inputSchema)

          val parameters = Trainable.Parameters(
            featureColumns = Some(MultipleColumnSelection(
              Vector(NameColumnSelection(Set("column1", "column0"))))),
            targetColumn = Some(NameSingleColumnSelection("column4")))

          val result = untrainedModel.train(context)(parameters)(dataFrame)
          validateResult(trainedModel, result, "column4")
      }

    "throw when target column is 3-level categorical" in {
      val dataFrame = createDataFrame(inputRows, inputSchema, Seq("column2"))

      a[WrongColumnTypeException] shouldBe thrownBy {
        val regression = constructUntrainedModel(mockUntrainedModel())
        val parameters = Trainable.Parameters(
          featureColumns = Some(MultipleColumnSelection(
            Vector(NameColumnSelection(Set("column1", "column0"))))),
          targetColumn = Some(NameSingleColumnSelection("column2")))
        regression.train(executionContext)(parameters)(dataFrame)
      }
    }

  }
}
