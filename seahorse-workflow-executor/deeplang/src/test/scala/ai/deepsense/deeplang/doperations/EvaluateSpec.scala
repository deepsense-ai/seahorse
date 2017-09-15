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

import ai.deepsense.deeplang._
import ai.deepsense.deeplang.doperables.MetricValue
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperations.MockDOperablesFactory._
import ai.deepsense.deeplang.doperations.exceptions.TooManyPossibleTypesException
import ai.deepsense.deeplang.exceptions.DeepLangMultiException
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import ai.deepsense.deeplang.params.ParamsMatchers._

class EvaluateSpec extends UnitSpec with DeeplangTestSupport {

  "Evaluate" should {

    "evaluate input Evaluator on input DataFrame with proper parameters set" in {
      val evaluator = new MockEvaluator

      def testEvaluate(op: Evaluate, expected: MetricValue): Unit = {
        val Vector(outputDataFrame) = op.executeUntyped(Vector(evaluator, mock[DataFrame]))(createExecutionContext)
        outputDataFrame shouldBe expected
      }

      val op1 = Evaluate()
      testEvaluate(op1, metricValue1)

      val paramsForEvaluator = JsObject(evaluator.paramA.name -> JsNumber(2))
      val op2 = Evaluate().setEvaluatorParams(paramsForEvaluator)
      testEvaluate(op2, metricValue2)
    }

    "not modify params in input Evaluator instance upon execution" in {
      val evaluator = new MockEvaluator
      val originalEvaluator = evaluator.replicate()

      val paramsForEvaluator = JsObject(evaluator.paramA.name -> JsNumber(2))
      val op = Evaluate().setEvaluatorParams(paramsForEvaluator)
      op.executeUntyped(Vector(evaluator, mock[DataFrame]))(createExecutionContext)

      evaluator should have (theSameParamsAs (originalEvaluator))
    }

    "infer knowledge from input Evaluator on input DataFrame with proper parameters set" in {
      val evaluator = new MockEvaluator

      def testInference(op: Evaluate, expectedKnowledge: DKnowledge[MetricValue]): Unit = {
        val inputDF = DataFrame.forInference(createSchema())
        val (knowledge, warnings) = op.inferKnowledgeUntyped(
          Vector(DKnowledge(evaluator), DKnowledge(inputDF)))(mock[InferContext])
        // Currently, InferenceWarnings are always empty.
        warnings shouldBe InferenceWarnings.empty
        val Vector(dataFrameKnowledge) = knowledge
        dataFrameKnowledge shouldBe expectedKnowledge
      }

      val op1 = Evaluate()
      testInference(op1, metricValueKnowledge1)

      val paramsForEvaluator = JsObject(evaluator.paramA.name -> JsNumber(2))
      val op2 = Evaluate().setEvaluatorParams(paramsForEvaluator)
      testInference(op2, metricValueKnowledge2)
    }

    "not modify params in input Evaluator instance upon inference" in {
      val evaluator = new MockEvaluator
      val originalEvaluator = evaluator.replicate()

      val paramsForEvaluator = JsObject(evaluator.paramA.name -> JsNumber(2))
      val op = Evaluate().setEvaluatorParams(paramsForEvaluator)
      val inputDF = DataFrame.forInference(createSchema())
      op.inferKnowledgeUntyped(Vector(DKnowledge(evaluator), DKnowledge(inputDF)))(mock[InferContext])

      evaluator should have (theSameParamsAs (originalEvaluator))
    }

    "throw Exception" when {
      "there is more than one Evaluator in input Knowledge" in {
        val inputDF = DataFrame.forInference(createSchema())
        val evaluators = Set[DOperable](new MockEvaluator, new MockEvaluator)

        val op = Evaluate()
        a [TooManyPossibleTypesException] shouldBe thrownBy {
          op.inferKnowledgeUntyped(Vector(DKnowledge(evaluators), DKnowledge(inputDF)))(mock[InferContext])
        }
      }
      "values of dynamic parameters are invalid" in {
        val evaluator = new MockEvaluator

        val inputDF = DataFrame.forInference(createSchema())

        val paramsForEvaluator = JsObject(evaluator.paramA.name -> JsNumber(-2))
        val evaluatorWithParams = Evaluate().setEvaluatorParams(paramsForEvaluator)

        a [DeepLangMultiException] shouldBe thrownBy {
          evaluatorWithParams.inferKnowledgeUntyped(
            Vector(DKnowledge(evaluator), DKnowledge(inputDF)))(mock[InferContext])
        }
      }
    }
  }
}
