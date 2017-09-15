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

import org.mockito.Mockito._
import org.mockito.Matchers._

import io.deepsense.deeplang.doperables.Scorable
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.inference.{InferenceWarning, InferenceWarnings, InferContext}
import io.deepsense.deeplang._

abstract class ScorerSpec[T <: Scorable : Manifest] extends UnitSpec {

  protected def scorer: DOperation

  protected def scorerName: String

  protected def predictionColumnName = "prediction"

  scorerName should {
    "score model on DataFrame" in {
      val scorableMock = mock[T]
      val scoreMethodMock = mock[DMethod1To1[String, DataFrame, DataFrame]]
      val executionContextStub = mock[ExecutionContext]
      val dataframeStub = mock[DataFrame]
      val resultDataframeStub = mock[DataFrame]

      when(scorableMock.score).thenReturn(scoreMethodMock)
      when(scoreMethodMock.apply(
        executionContextStub)(predictionColumnName)(dataframeStub)).thenReturn(resultDataframeStub)

      val result = scorer.execute(executionContextStub)(Vector(scorableMock, dataframeStub))
      result shouldBe Vector(resultDataframeStub)
    }
  }

  it should {
    "infer result DataFrame correctly" in {
      val dataFrameKnowledgeStub = mock[DKnowledge[DataFrame]]
      val inferContextStub = mock[InferContext]
      when(inferContextStub.fullInference).thenReturn(true)

      def scorableMock(): (T, (DKnowledge[DOperable], InferenceWarnings)) = {
        val mocked = mock[T]
        val scoreMethodMock = mock[DMethod1To1[String, DataFrame, DataFrame]]
        val resultDataframeStub = mock[DataFrame]
        val resultDKnowledgeStub = DKnowledge(resultDataframeStub)
        val warningsStub = InferenceWarnings(mock[InferenceWarning])
        when(mocked.score).thenReturn(scoreMethodMock)
        when(scoreMethodMock.infer(inferContextStub)(predictionColumnName)(dataFrameKnowledgeStub))
          .thenReturn((resultDKnowledgeStub, warningsStub))
        (mocked, (resultDKnowledgeStub, warningsStub))
      }
      val (scorableMock1, (scorableResult1, warning1)) = scorableMock()
      val (scorableMock2, (scorableResult2, warning2)) = scorableMock()
      val inputKnowledge = Vector(
        DKnowledge[DOperable](scorableMock1, scorableMock2),
        dataFrameKnowledgeStub.asInstanceOf[DKnowledge[DOperable]])

      val (dKnowledge, warnings) = scorer.inferKnowledge(inferContextStub)(inputKnowledge)
      dKnowledge shouldBe Vector(scorableResult1 ++ scorableResult2)
      warnings shouldBe (warning1 ++ warning2)
    }
  }

}
