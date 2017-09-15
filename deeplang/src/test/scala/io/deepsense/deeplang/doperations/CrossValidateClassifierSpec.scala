/**
 * Copyright 2015, CodiLime Inc.
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
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar

import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.parameters.{MultipleColumnSelection, SingleColumnSelection}
import io.deepsense.deeplang.{DKnowledge, DMethod1To1, ExecutionContext, UnitSpec}
import io.deepsense.reportlib.model.ReportContent

/**
 * Tests only mocked model trained by CrossValidateClassifier (ignores report)
 */
class CrossValidateClassifierSpec extends UnitSpec with MockitoSugar {

  val classifier = new CrossValidateClassifier
  // NOTE: When folds == 0, only training is performed (returned report is empty)
  classifier.numberOfFoldsParameter.value = Some(0.0)
  classifier.shuffleParameter.value = Some(CrossValidate.BinaryChoice.NO.toString)
  classifier.seedShuffleParameter.value = Some(0.0)

  val trainableParametersStub = Trainable.Parameters(
    Some(mock[MultipleColumnSelection]), Some(mock[SingleColumnSelection]))

  classifier.featureColumnsParameter.value = trainableParametersStub.featureColumns
  classifier.targetColumnParameter.value = trainableParametersStub.targetColumn

  "CrossValidateClassifier with parameters set" should {
    "train untrained model on DataFrame" in {
      val trainableMock = mock[UntrainedLogisticRegression]
      val trainMethodMock = mock[DMethod1To1[Trainable.Parameters, DataFrame, Scorable]]

      val executionContextStub = mock[ExecutionContext]
      val scorableStub = mock[TrainedLogisticRegression]
      val dataframeStub = mock[DataFrame]
      val dataframeSparkStub = mock[sql.DataFrame]
      when(dataframeStub.sparkDataFrame).thenReturn(dataframeSparkStub)
      when(dataframeSparkStub.count()).thenReturn(1L)

      when(trainableMock.train).thenReturn(trainMethodMock)
      when(trainMethodMock.apply(
        executionContextStub)(trainableParametersStub)(dataframeStub)).thenReturn(scorableStub)

      val result = classifier.execute(executionContextStub)(Vector(trainableMock, dataframeStub))

      result shouldBe Vector(
        scorableStub, Report(ReportContent("Cross-validate Classification Report")))
    }

    "infer results of it's types" in {
      val classifierWithoutParams = new CrossValidateClassifier
      val inferContextStub = mock[InferContext]
      val dDOperableCatalogMock = mock[DOperableCatalog]
      when(inferContextStub.dOperableCatalog).thenReturn(dDOperableCatalogMock)
      // NOTE: We are also checking that mockedReportSet is returned by inferKnowledge
      val mockedReportSet = Set[Report]()
      when(dDOperableCatalogMock.concreteSubclassesInstances[Report]).thenReturn(mockedReportSet)

      val scorableStubs = Vector(mock[Scorable], mock[Scorable], mock[Scorable])
      val scorableKnowledgeStub1 = DKnowledge(Set(scorableStubs(0), scorableStubs(1)))
      val scorableKnowledgeStub2 = DKnowledge(Set(scorableStubs(1), scorableStubs(2)))
      val dataframeKnowledgeStub = mock[DKnowledge[DataFrame]]

      val trainableMock1 = mock[UntrainedLogisticRegression]
      val trainMethodMock1 = mock[DMethod1To1[Trainable.Parameters, DataFrame, Scorable]]

      val trainableMock2 = mock[UntrainedLogisticRegression]
      val trainMethodMock2 = mock[DMethod1To1[Trainable.Parameters, DataFrame, Scorable]]

      when(trainableMock1.train).thenReturn(trainMethodMock1)
      when(trainMethodMock1.infer(any())(any())(any()))
        .thenReturn((scorableKnowledgeStub1, InferenceWarnings.empty))

      when(trainableMock2.train).thenReturn(trainMethodMock2)
      when(trainMethodMock2.infer(any())(any())(any()))
        .thenReturn((scorableKnowledgeStub2, InferenceWarnings.empty))

      val (result, _) = classifierWithoutParams.inferKnowledge(
        inferContextStub)(
            Vector(DKnowledge(trainableMock1, trainableMock2), dataframeKnowledgeStub))

      result.head shouldBe DKnowledge(scorableStubs.toSet)
      result.size shouldBe 2
    }
  }
}
