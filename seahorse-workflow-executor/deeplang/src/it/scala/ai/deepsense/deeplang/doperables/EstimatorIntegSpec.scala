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
import ai.deepsense.deeplang.{DKnowledge, DeeplangTestSupport, ExecutionContext, UnitSpec}

class EstimatorIntegSpec extends UnitSpec with DeeplangTestSupport {

  private def estimator = {
    val e = mock[Estimator[Transformer]]
    when(e.fit) thenCallRealMethod()
    e
  }

  val transformer = mock[Transformer]

  "Estimator" should {
    "fit to a DataFrame producing a Transfomer" in {
      val dataFrame: DataFrame = mock[DataFrame]
      val e = estimator
      val context: ExecutionContext = mock[ExecutionContext]
      when(e._fit(context, dataFrame)).thenReturn(transformer)
      val outputTransfomer = e.fit(context)(())(dataFrame)
      outputTransfomer shouldBe transformer
    }
    "infer" when {
      "input DKnowledge contains exactly one type" in {
        val schema = createSchema()
        val inputDKnowledge = DKnowledge(
          DataFrame.forInference(schema)
        )
        val e = estimator
        when(e._fit_infer(Some(schema))).thenReturn(transformer)
        val (outputKnowledge, warnings) = e.fit.infer(mock[InferContext])(())(inputDKnowledge)
        outputKnowledge shouldBe DKnowledge(transformer)
        warnings shouldBe InferenceWarnings.empty
      }
      "input DKnowledge contains more than one type (by taking the first type)" in {
        val schema = createSchema()
        val schema2 = createSchema()
        val inputDKnowledge = DKnowledge(
          DataFrame.forInference(schema),
          DataFrame.forInference(schema2)
        )
        val e = estimator
        when(e._fit_infer(Some(schema))).thenReturn(transformer)
        val (outputKnowledge, warnings) = e.fit.infer(mock[InferContext])(())(inputDKnowledge)
        outputKnowledge shouldBe DKnowledge(transformer)
        warnings shouldBe InferenceWarnings.empty
      }
    }
  }
}
