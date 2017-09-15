/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import org.mockito.Mockito._

import io.deepsense.deeplang.doperables.Scorable
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.{DMethod1To1, DOperation, ExecutionContext, UnitSpec}

abstract class ScorerSpec[T <: Scorable : Manifest] extends UnitSpec {

  protected def scorer: DOperation

  protected def scorerName: String

  scorerName should {
    "score model on dataframe" in {
      val scorableMock = mock[T]
      val scoreMethodMock = mock[DMethod1To1[Unit, DataFrame, DataFrame]]
      val executionContextStub = mock[ExecutionContext]
      val dataframeStub = mock[DataFrame]
      val resultDataframeStub = mock[DataFrame]

      when(scorableMock.score).thenReturn(scoreMethodMock)
      when(scoreMethodMock.apply(
        executionContextStub)(())(dataframeStub)).thenReturn(resultDataframeStub)

      val result = scorer.execute(executionContextStub)(Vector(scorableMock, dataframeStub))
      result shouldBe Vector(resultDataframeStub)
    }
  }
}
