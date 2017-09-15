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

package io.deepsense.deeplang.doperations

import org.apache.spark.sql
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar

import io.deepsense.deeplang._
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.machinelearning.ridgeregression.{TrainedRidgeRegression, UntrainedRidgeRegression}
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.parameters.ChoiceParameter.BinaryChoice
import io.deepsense.deeplang.parameters.{MultipleColumnSelection, SingleColumnSelection}
import io.deepsense.reportlib.model.ReportContent

/**
 * Tests only mocked model trained by CrossValidateRegressor (ignores report)
 */
class CrossValidateRegressorSpec extends UnitSpec with MockitoSugar {

  val regressor = new CrossValidateRegressor
  // NOTE: When folds == 0, only regression train is performed (returned report is empty)
  regressor.numberOfFoldsParameter.value = 0.0
  regressor.shuffleParameter.value = BinaryChoice.NO.toString
  regressor.seedShuffleParameter.value = 0.0

  regressor.featureColumnsParameter.value = mock[MultipleColumnSelection]
  regressor.targetColumnParameter.value = mock[SingleColumnSelection]

  "CrossValidateRegressor with parameters set" should {
    "train untrained model on DataFrame" in {
      val trainableMock = mock[UntrainedRidgeRegression]
      val trainMethodMock = mock[DMethod1To1[TrainableParameters, DataFrame, Scorable]]

      val executionContextStub = mock[ExecutionContext]
      val scorableStub = mock[TrainedRidgeRegression]
      val dataframeStub = mock[DataFrame]
      val dataframeSparkStub = mock[sql.DataFrame]
      when(dataframeStub.sparkDataFrame).thenReturn(dataframeSparkStub)
      when(dataframeSparkStub.count()).thenReturn(1L)

      when(trainableMock.train).thenReturn(trainMethodMock)
      when(trainMethodMock.apply(
        executionContextStub)(regressor)(dataframeStub)).thenReturn(scorableStub)

      val result = regressor.execute(executionContextStub)(Vector(trainableMock, dataframeStub))

      result shouldBe Vector(
        scorableStub, Report(ReportContent("Cross-validate Regression Report")))
    }

    "infer results of it's types" in {
      val regressorWithoutParams = new CrossValidateRegressor
      val inferContextStub = mock[InferContext]
      val dDOperableCatalogMock = mock[DOperableCatalog]
      when(inferContextStub.dOperableCatalog).thenReturn(dDOperableCatalogMock)
      // NOTE: We are also checking that mockedReportSet is returned by inferKnowledge
      val mockedReportSet = Set[Report]()
      when(dDOperableCatalogMock.concreteSubclassesInstances[Report]).thenReturn(mockedReportSet)

      val scorableStubs = Vector.fill(3)(mock[Scorable])
      val scorableKnowledgeStub1 = DKnowledge(Set(scorableStubs(0), scorableStubs(1)))
      val scorableKnowledgeStub2 = DKnowledge(Set(scorableStubs(1), scorableStubs(2)))
      val dataframeKnowledgeStub = mock[DKnowledge[DataFrame]]

      val trainableMock1 = mock[UntrainedRidgeRegression]
      val trainMethodMock1 = mock[DMethod1To1[TrainableParameters, DataFrame, Scorable]]

      val trainableMock2 = mock[UntrainedRidgeRegression]
      val trainMethodMock2 = mock[DMethod1To1[TrainableParameters, DataFrame, Scorable]]
      when(trainableMock1.train).thenReturn(trainMethodMock1)
      when(trainMethodMock1.infer(any())(any())(any()))
        .thenReturn((scorableKnowledgeStub1, InferenceWarnings.empty))
      when(trainableMock2.train).thenReturn(trainMethodMock2)
      when(trainMethodMock2.infer(any())(any())(any()))
        .thenReturn((scorableKnowledgeStub2, InferenceWarnings.empty))
      val (result, _) = regressorWithoutParams.inferKnowledge(
        inferContextStub)(
          Vector(DKnowledge(trainableMock1, trainableMock2), dataframeKnowledgeStub))
      result.head shouldBe DKnowledge(scorableStubs.toSet)
      result.size shouldBe 2
    }
  }
}
