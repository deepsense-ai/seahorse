/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar

import io.deepsense.deeplang.{DKnowledge, DMethod1To1, ExecutionContext, InferContext, UnitSpec}
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters.{MultipleColumnSelection, SingleColumnSelection}

class TrainClassifierSpec extends UnitSpec with MockitoSugar {

  val classifier = TrainClassifier()

  val trainableParametersStub = Trainable.Parameters(
    Some(mock[MultipleColumnSelection]), Some(mock[SingleColumnSelection]))

  classifier.parameters.
    getSingleColumnSelectorParameter("target column").value = trainableParametersStub.targetColumn

  classifier.parameters.
    getColumnSelectorParameter("feature columns").value = trainableParametersStub.featureColumns

  "TrainClassifier with parameters set" should {
    "train untrained model on dataframe" in {
      val trainableMock = mock[Trainable with Classifier]
      val trainMethodMock = mock[DMethod1To1[Trainable.Parameters, DataFrame, Scorable]]

      val executionContextStub = mock[ExecutionContext]
      val scorableStub = mock[Scorable]
      val dataframeStub = mock[DataFrame]

      when(trainableMock.train).thenReturn(trainMethodMock)
      when(trainMethodMock.apply(
        executionContextStub)(trainableParametersStub)(dataframeStub)).thenReturn(scorableStub)

      val result = classifier.execute(executionContextStub)(Vector(trainableMock, dataframeStub))

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

      val trainableMock1 = mock[Trainable with Classifier]
      val trainMethodMock1 = mock[DMethod1To1[Trainable.Parameters, DataFrame, Scorable]]

      val trainableMock2 = mock[Trainable with Classifier]
      val trainMethodMock2 = mock[DMethod1To1[Trainable.Parameters, DataFrame, Scorable]]

      when(trainableMock1.train).thenReturn(trainMethodMock1)
      when(trainMethodMock1.infer(any())(any())(any())).thenReturn(scorableKnowledgeStub1)

      when(trainableMock2.train).thenReturn(trainMethodMock2)
      when(trainMethodMock2.infer(any())(any())(any())).thenReturn(scorableKnowledgeStub2)

      val result = classifier.inferKnowledge(inferContextStub)(
        Vector(DKnowledge(trainableMock1, trainableMock2), dataframeKnowledgeStub))

      result shouldBe Vector(DKnowledge(scorableStubs.toSet))
    }
  }
}
