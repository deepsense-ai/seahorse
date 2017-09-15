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

import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.{ColumnMetadata, DataFrame, DataFrameMetadata}
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.parameters.MultipleColumnSelection
import io.deepsense.deeplang.{DKnowledge, DMethod1To1, ExecutionContext, UnitSpec}

class TrainClusteringSpec extends UnitSpec with MockitoSugar {

  val clustering = TrainClustering()

  clustering.predictionColumnParameter.value = "prediction"
  clustering.featureColumnsParameter.value = mock[MultipleColumnSelection]

  "TrainClustering with parameters set" should {
    "train untrained model on dataframe" in {
      val trainableMock =
        mock[UnsupervisedTrainable with Clustering]
      val trainMethodMock =
        mock[DMethod1To1[UnsupervisedTrainableParameters, DataFrame, Scorable]]
      val scoreMethodMock =
        mock[DMethod1To1[String, DataFrame, DataFrame]]

      val executionContextStub = mock[ExecutionContext]
      val scorableStub = mock[Scorable]
      val dataframeStub = mock[DataFrame]
      val resultDataFrameStub = mock[DataFrame]

      when(scorableStub.score).thenReturn(scoreMethodMock)
      when(scoreMethodMock.apply(any())(any())(any())).thenReturn(resultDataFrameStub)

      when(trainableMock.train).thenReturn(trainMethodMock)
      when(trainMethodMock.apply(
        executionContextStub)(clustering)(dataframeStub)).thenReturn(scorableStub)

      val result = clustering.execute(executionContextStub)(Vector(trainableMock, dataframeStub))

      result shouldBe Vector(scorableStub, resultDataFrameStub)
    }
  }

  it should {
    "infer results of its types" in {
      val inferContextStub = mock[InferContext]
      val scorableStubs = Vector.fill(3)(mock[Scorable])
      val scorableKnowledgeStub1 = DKnowledge(Set(scorableStubs(0), scorableStubs(1)))
      val scorableKnowledgeStub2 = DKnowledge(Set(scorableStubs(1), scorableStubs(2)))
      val dataframeKnowledgeStub = mock[DKnowledge[DataFrame]]

      val inferKnowledgeStub = mock[DataFrame]
      val metadataStub = mock[DataFrameMetadata]

      when(inferKnowledgeStub.inferredMetadata).thenReturn(Some(metadataStub))
      when(metadataStub.isExact).thenReturn(false)
      when(metadataStub.isColumnCountExact).thenReturn(false)
      when(metadataStub.columns).thenReturn(Map[String, ColumnMetadata]())

      when(dataframeKnowledgeStub.types).thenReturn(Set(inferKnowledgeStub))

      val trainableMock1 =
        mock[UnsupervisedTrainable with Clustering]
      val trainMethodMock1 =
        mock[DMethod1To1[UnsupervisedTrainableParameters, DataFrame, Scorable]]

      val trainableMock2 =
        mock[UnsupervisedTrainable with Clustering]
      val trainMethodMock2 =
        mock[DMethod1To1[UnsupervisedTrainableParameters, DataFrame, Scorable]]

      when(trainableMock1.train).thenReturn(trainMethodMock1)
      when(trainMethodMock1.infer(any())(any())(any()))
        .thenReturn((scorableKnowledgeStub1, InferenceWarnings.empty))

      when(trainableMock2.train).thenReturn(trainMethodMock2)
      when(trainMethodMock2.infer(any())(any())(any()))
        .thenReturn((scorableKnowledgeStub2, InferenceWarnings.empty))

      val (result, _) = clustering.inferKnowledge(inferContextStub)(
        Vector(DKnowledge(trainableMock1, trainableMock2), dataframeKnowledgeStub))

      result(0) shouldBe DKnowledge(scorableStubs.toSet)
      result(1).types.head.isInstanceOf[DataFrame] shouldBe true
    }
  }
}
