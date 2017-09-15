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

import org.apache.spark.sql.types.StructType
import spray.json.{JsNumber, JsObject}

import io.deepsense.deeplang.doperables.Transformer
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.MockDOperablesFactory._
import io.deepsense.deeplang.doperations.exceptions.TooManyPossibleTypesException
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.params.ParamsMatchers._
import io.deepsense.deeplang.{DKnowledge, DOperable, ExecutionContext, UnitSpec}

class FitSpec extends UnitSpec {

  "Fit" should {
    "fit input Estimator on input DataFrame with proper parameters set" in {
      val estimator = new MockEstimator

      def testFit(op: Fit, expectedTransformer: Transformer): Unit = {
        val Vector(outputTransformer: Transformer) =
          op.execute(mock[ExecutionContext])(Vector(estimator, mock[DataFrame]))
        outputTransformer shouldBe expectedTransformer
      }
      val op1 = Fit()
      testFit(op1, transformer1)

      val paramsForEstimator = JsObject(estimator.paramA.name -> JsNumber(2))
      val op2 = Fit().setEstimatorParams(paramsForEstimator)
      testFit(op2, transformer2)
    }
    "not modify params in input Estimator instance upon execution" in {
      val estimator = new MockEstimator
      val originalEstimator = estimator.replicate()

      val paramsForEstimator = JsObject(estimator.paramA.name -> JsNumber(2))
      val op = Fit().setEstimatorParams(paramsForEstimator)
      op.execute(mock[ExecutionContext])(Vector(estimator, mock[DataFrame]))

      estimator should have (theSameParamsAs (originalEstimator))
    }
    "infer Transformer from input Estimator on input DataFrame with proper parameters set" in {
      val estimator = new MockEstimator

      def testInference(op: Fit, expectedTransformerKnowledge: DKnowledge[Transformer]): Unit = {
        val inputDF = DataFrame.forInference(mock[StructType])
        val (knowledge, warnings) =
          op.inferKnowledge(mock[InferContext])(Vector(DKnowledge(estimator), DKnowledge(inputDF)))
        // Currently, InferenceWarnings are always empty.
        warnings shouldBe InferenceWarnings.empty
        val Vector(transformerKnowledge) = knowledge
        transformerKnowledge shouldBe expectedTransformerKnowledge
      }
      val op1 = Fit()
      testInference(op1, transformerKnowledge1)

      val paramsForEstimator = JsObject(estimator.paramA.name -> JsNumber(2))
      val op2 = Fit().setEstimatorParams(paramsForEstimator)
      testInference(op2, transformerKnowledge2)
    }
    "not modify params in input Estimator instance upon inference" in {
      val estimator = new MockEstimator
      val originalEstimator = estimator.replicate()

      val paramsForEstimator = JsObject(estimator.paramA.name -> JsNumber(2))
      val op = Fit().setEstimatorParams(paramsForEstimator)
      val inputDF = DataFrame.forInference(mock[StructType])
      op.inferKnowledge(mock[InferContext])(Vector(DKnowledge(estimator), DKnowledge(inputDF)))

      estimator should have (theSameParamsAs (originalEstimator))
    }
    "throw Exception" when {
      "there are more than one Estimator in input Knowledge" in {
        val inputDF = DataFrame.forInference(mock[StructType])
        val estimators = Set[DOperable](new MockEstimator, new MockEstimator)

        val op = Fit()
        a[TooManyPossibleTypesException] shouldBe thrownBy {
          op.inferKnowledge(mock[InferContext])(Vector(DKnowledge(estimators), DKnowledge(inputDF)))
        }
      }
    }
  }
}
