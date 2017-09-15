/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.doperations

import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.{Scorable, Trainable}
import io.deepsense.deeplang.parameters.{MultipleColumnSelection, SingleColumnSelection}

class TrainRegressorSpec extends UnitSpec with MockitoSugar {

  val regressor = new TrainRegressor

  val trainableParametersStub = Trainable.Parameters(
    mock[MultipleColumnSelection], mock[SingleColumnSelection])

  regressor.parameters.
    getSingleColumnSelectorParameter("target column").value =
      Some(trainableParametersStub.targetColumn)

  regressor.parameters.
    getColumnSelectorParameter("feature columns").value =
    Some(trainableParametersStub.featureColumns)

  "TrainRegressor with parameters set" should "train untrained model on dataframe" in {
    val trainableMock = new Trainable {
      override val train = mock[DMethod1To1[Trainable.Parameters, DataFrame, Scorable]]
    }
    val executionContextStub = mock[ExecutionContext]
    val scorableStub = mock[Scorable]
    val dataframeStub = mock[DataFrame]

    when(trainableMock.train.apply(
      executionContextStub)(trainableParametersStub)(dataframeStub)).thenReturn(scorableStub)

    val result = regressor.execute(executionContextStub)(Vector(trainableMock, dataframeStub))

    result shouldBe Vector(scorableStub)
  }

  it should "infer results of it's types" in {
    val inferContextStub = mock[InferContext]
    val scorableStubs = Vector(mock[Scorable], mock[Scorable], mock[Scorable])
    val scorableKnowledgeStub1 = DKnowledge(Set(scorableStubs(0), scorableStubs(1)))
    val scorableKnowledgeStub2 = DKnowledge(Set(scorableStubs(1), scorableStubs(2)))
    val dataframeKnowledgeStub = mock[DKnowledge[DataFrame]]

    val trainableMock1 = new Trainable {
      override val train = mock[DMethod1To1[Trainable.Parameters, DataFrame, Scorable]]
    }
    val trainableMock2 = new Trainable {
      override val train = mock[DMethod1To1[Trainable.Parameters, DataFrame, Scorable]]
    }

    when(trainableMock1.train.infer(any())(any())(any())).thenReturn(scorableKnowledgeStub1)
    when(trainableMock2.train.infer(any())(any())(any())).thenReturn(scorableKnowledgeStub2)

    val result = regressor.inferKnowledge(
      inferContextStub)(Vector(DKnowledge(trainableMock1, trainableMock2), dataframeKnowledgeStub))

    result shouldBe Vector(DKnowledge(scorableStubs.toSet))
  }
}
