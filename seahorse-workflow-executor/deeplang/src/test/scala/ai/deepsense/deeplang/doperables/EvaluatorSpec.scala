/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperables

import org.mockito.Mockito._

import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import ai.deepsense.deeplang.{DKnowledge, ExecutionContext, UnitSpec}

class EvaluatorSpec extends UnitSpec {

  private def evaluator = {
    val e = mock[Evaluator]
    when(e.evaluate) thenCallRealMethod()
    e
  }

  val dataFrame = mock[DataFrame]
  val metricValue = mock[MetricValue]

  val execCtx = mock[ExecutionContext]
  val inferCtx = mock[InferContext]

  val emptyWarnings = InferenceWarnings.empty

  "Evaluator" should {

    "evaluate DataFrame" in {
      val e = evaluator

      when(e._evaluate(execCtx, dataFrame)) thenReturn metricValue
      e.evaluate(execCtx)(())(dataFrame) shouldBe metricValue
    }

    "infer knowledge" in {
      val e = evaluator
      when(e._infer(DKnowledge(dataFrame))) thenReturn metricValue

      val (knowledge, warnings) = e.evaluate.infer(inferCtx)(())(DKnowledge(dataFrame))
      knowledge.single shouldBe metricValue
      warnings shouldBe emptyWarnings
    }
  }
}
