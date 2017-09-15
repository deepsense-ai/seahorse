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

package ai.deepsense.deeplang.doperations

import spray.json.{JsNumber, JsObject}

import ai.deepsense.deeplang.doperables.Transformer
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperations.MockDOperablesFactory._
import ai.deepsense.deeplang.doperations.exceptions.TooManyPossibleTypesException
import ai.deepsense.deeplang.exceptions.DeepLangMultiException
import ai.deepsense.deeplang.inference.InferContext
import ai.deepsense.deeplang._

class FitPlusTransformSpec extends UnitSpec with DeeplangTestSupport {

  "FitPlusTransform" when {
    "executed" should {
      "pass parameters to the input Estimator produce a Transformer and transformed DataFrame" in {
        val estimator = new MockEstimator
        val initialParametersValues = estimator.extractParamMap()
        val fpt = new FitPlusTransform

        def testExecute(
          op: FitPlusTransform,
          expectedDataFrame: DataFrame,
          expectedTransformer: Transformer): Unit = {
          val results = op.executeUntyped(Vector(estimator, mock[DataFrame]))(createExecutionContext)
          val outputDataFrame = results(0).asInstanceOf[DataFrame]
          val outputTransformer = results(1).asInstanceOf[Transformer]

          outputDataFrame shouldBe expectedDataFrame
          outputTransformer shouldBe expectedTransformer
        }

        testExecute(fpt, transformedDataFrame1, transformer1)
        fpt.setEstimatorParams(JsObject(estimator.paramA.name -> JsNumber(2)))
        testExecute(fpt, transformedDataFrame2, transformer2)
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
          expectedDataFrameKnowledge: DKnowledge[DataFrame],
          expectedTransformerKnowledge: DKnowledge[Transformer]): Unit = {
          val (Vector(outputDataFrameKnowledge, outputTransformerKnowledge), _) =
            op.inferKnowledgeUntyped(Vector(DKnowledge(estimator), mock[DKnowledge[DataFrame]]))(mock[InferContext])

          outputDataFrameKnowledge shouldBe expectedDataFrameKnowledge
          outputTransformerKnowledge shouldBe expectedTransformerKnowledge
        }

        testInference(fpt, transformedDataFrameKnowledge1, transformerKnowledge1)
        fpt.setEstimatorParams(JsObject(estimator.paramA.name -> JsNumber(2)))
        testInference(fpt, transformedDataFrameKnowledge2, transformerKnowledge2)
        estimator.extractParamMap() shouldBe initialParametersValues
      }
      "throw exceptions" when {
        "input Estimator Knowledge consist more than one type" in {
          val estimators = Set[DOperable](new MockEstimator, new MockEstimator)
          val inputKnowledge: Vector[DKnowledge[DOperable]] =
            Vector(DKnowledge(estimators), mock[DKnowledge[DataFrame]])
          val fpt = new FitPlusTransform
          a[TooManyPossibleTypesException] shouldBe thrownBy {
            fpt.inferKnowledgeUntyped(inputKnowledge)(mock[InferContext])
          }
        }
        "Estimator's dynamic parameters are invalid" in {
          val estimator = new MockEstimator
          val inputKnowledge: Vector[DKnowledge[DOperable]] =
            Vector(DKnowledge(estimator), mock[DKnowledge[DataFrame]])
          val fpt = new FitPlusTransform
          fpt.setEstimatorParams(JsObject(estimator.paramA.name -> JsNumber(-2)))
          a[DeepLangMultiException] shouldBe thrownBy {
            fpt.inferKnowledgeUntyped(inputKnowledge)(mock[InferContext])
          }
        }
      }
    }
  }
}
