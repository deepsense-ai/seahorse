/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Chilkiewicz
 */
package io.deepsense.deeplang.doperations

import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.Scorable
import io.deepsense.deeplang.doperables.dataframe.DataFrame

class ScoreRegressorSpec extends UnitSpec with MockitoSugar {

  val scoreRegressor = new ScoreRegressor

  "ScoreRegressor" should "score model on dataframe" in {
    val scorableMock = new Scorable {
      override val score = mock[DMethod1To1[Unit, DataFrame, DataFrame]]
    }
    val executionContextStub = mock[ExecutionContext]
    val dataframeStub = mock[DataFrame]
    val resultDataframeStub = mock[DataFrame]

    when(scorableMock.score.apply(
      executionContextStub)(())(dataframeStub)).thenReturn(resultDataframeStub)

    val result = scoreRegressor.execute(executionContextStub)(Vector(scorableMock, dataframeStub))

    result shouldBe Vector(resultDataframeStub)
  }
}
