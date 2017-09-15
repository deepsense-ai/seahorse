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

import spray.json.{JsNumber, JsObject}

import io.deepsense.deeplang.doperables.Transformer
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.MockDOperablesFactory._
import io.deepsense.deeplang.doperations.exceptions.TooManyPossibleTypesException
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.deeplang.{DKnowledge, DOperable, ExecutionContext, UnitSpec}

class FitPlusTransformSpec extends UnitSpec {

  "FitPlusTransform" when {
    "executed" should {
      "pass parameters to the input Estimator produce a Transformer and transformed DataFrame" in {
        val estimator = new MockEstimator
        val initialParametersValues = estimator.extractParamMap()
        val fpt = new FitPlusTransform

        def testExecute(
          op: FitPlusTransform,
          expectedTransformer: Transformer,
          expectedDataFrame: DataFrame): Unit = {
          val results = op.execute(mock[ExecutionContext])(Vector(estimator, mock[DataFrame]))
          val outputDataFrame = results(0).asInstanceOf[DataFrame]
          val outputTransformer = results(1).asInstanceOf[Transformer]

          outputDataFrame shouldBe expectedDataFrame
          outputTransformer shouldBe expectedTransformer
        }

        testExecute(fpt, transformer1, transformedDataFrame1)
        fpt.setEstimatorParams(JsObject(estimator.paramA.name -> JsNumber(2)))
        testExecute(fpt, transformer2, transformedDataFrame2)
        estimator.extractParamMap() shouldBe initialParametersValues
      }

    }
    "inferring knowledge" should {
      "take parameters from the input Estimator, infer Transformer and then a DataFrame" in {
        val estimator = new MockEstimator
        val initialParametersValues = estimator.extractParamMap()
        val fpt = new FitPlusTransform

        def testInference(
          op: FitPlusTransform,
          expectedTransformerKnowledge: DKnowledge[Transformer],
          expectedDataFrameKnowledge: DKnowledge[DataFrame]): Unit = {
          val (Vector(outputDataFrameKnowledge, outputTransformerKnowledge), _) =
            op.inferKnowledge(mock[InferContext])(
              Vector(DKnowledge(estimator), mock[DKnowledge[DataFrame]]))

          outputDataFrameKnowledge shouldBe expectedDataFrameKnowledge
          outputTransformerKnowledge shouldBe expectedTransformerKnowledge
        }

        testInference(fpt, transformerKnowledge1, transformedDataFrameKnowledge1)
        fpt.setEstimatorParams(JsObject(estimator.paramA.name -> JsNumber(2)))
        testInference(fpt, transformerKnowledge2, transformedDataFrameKnowledge2)
        estimator.extractParamMap() shouldBe initialParametersValues
      }
      "throw exceptions" when {
        "input Estimator Knowledge consist more than one type" in {
          val estimators = Set[DOperable](new MockEstimator, new MockEstimator)
          val inputKnowledge: Vector[DKnowledge[DOperable]] =
            Vector(DKnowledge(estimators), mock[DKnowledge[DataFrame]])
          val fpt = new FitPlusTransform
          a[TooManyPossibleTypesException] shouldBe thrownBy {
            fpt.inferKnowledge(mock[InferContext])(inputKnowledge)
          }
        }
      }
    }
  }
}
