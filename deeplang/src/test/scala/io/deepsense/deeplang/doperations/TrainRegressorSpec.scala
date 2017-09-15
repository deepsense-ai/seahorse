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

import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.machinelearning.ridgeregression.UntrainedRidgeRegression
import io.deepsense.deeplang.doperables.{TrainableParameters, TrainableParameters$, Scorable, Trainable}
import io.deepsense.deeplang.inference.{InferenceWarnings, InferContext}
import io.deepsense.deeplang.parameters.{MultipleColumnSelection, SingleColumnSelection}

class TrainRegressorSpec extends UnitSpec with MockitoSugar {

  val regressor = TrainRegressor()

  regressor.targetColumnParameter.value = mock[SingleColumnSelection]
  regressor.featureColumnsParameter.value = mock[MultipleColumnSelection]

  "TrainRegressor with parameters set" should {
    "train untrained model on dataframe" in {
      val trainableMock = mock[UntrainedRidgeRegression]
      val trainMethodMock = mock[DMethod1To1[TrainableParameters, DataFrame, Scorable]]

      val executionContextStub = mock[ExecutionContext]
      val scorableStub = mock[Scorable]
      val dataframeStub = mock[DataFrame]

      when(trainableMock.train).thenReturn(trainMethodMock)
      when(trainMethodMock.apply(
        executionContextStub)(regressor)(dataframeStub)).thenReturn(scorableStub)

      val result = regressor.execute(executionContextStub)(Vector(trainableMock, dataframeStub))

      result shouldBe Vector(scorableStub)
    }
  }

  it should {
    "infer results of its types" in {
      val inferContextStub = mock[InferContext]
      val scorableStubs = Vector(mock[Scorable], mock[Scorable], mock[Scorable])
      val scorableKnowledgeStub1 = DKnowledge(Set(scorableStubs(0), scorableStubs(1)))
      val scorableKnowledgeStub2 = DKnowledge(Set(scorableStubs(1), scorableStubs(2)))
      val dataframeKnowledgeStub = mock[DKnowledge[DataFrame]]

      val trainableMock1 = mock[UntrainedRidgeRegression]
      val trainMethodMock1 = mock[DMethod1To1[TrainableParameters, DataFrame, Scorable]]

      val trainableMock2 = mock[UntrainedRidgeRegression]
      val trainMethodMock2 = mock[DMethod1To1[TrainableParameters, DataFrame, Scorable]]

      when(trainableMock1.train).thenReturn(trainMethodMock1)
      when(trainMethodMock1.infer(any())(any())(any())).thenReturn(
        (scorableKnowledgeStub1, InferenceWarnings.empty))

      when(trainableMock2.train).thenReturn(trainMethodMock2)
      when(trainMethodMock2.infer(any())(any())(any())).thenReturn(
        (scorableKnowledgeStub2, InferenceWarnings.empty))

      val (result, warnings) = regressor.inferKnowledge(inferContextStub)(
        Vector(DKnowledge(trainableMock1, trainableMock2), dataframeKnowledgeStub))

      result shouldBe Vector(DKnowledge(scorableStubs.toSet))
      warnings shouldBe InferenceWarnings.empty
    }
  }
}
