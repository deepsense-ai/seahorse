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

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.MockDOperablesFactory._
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.params.ParamMap

class EstimatorAsOperationSpec extends UnitSpec {
  import EstimatorAsOperationSpec._

  "EstimatorAsOperation" should {
    def createMockOperation: MockEstimatorOperation = new MockEstimatorOperation
    "have the same parameters as the Estimator" in {
      val op = createMockOperation
      op.params shouldBe op.estimator.params
    }
   "have the same default values for parameters as Estimator" in {
     val op = createMockOperation
     op.extractParamMap() shouldBe ParamMap(createMockOperation.estimator.paramA -> DefaultForA)
    }
    "execute fit using properly set parameters" in {
      def testFit(
          op: MockEstimatorOperation,
          expectedDataFrame: DataFrame,
          expectedTransformer: Transformer): Unit = {
        val Vector(outputDataFrame: DataFrame, outputTransformer: Transformer) =
          op.execute(mock[ExecutionContext])(Vector(mock[DataFrame]))
        outputDataFrame shouldBe expectedDataFrame
        outputTransformer shouldBe expectedTransformer
      }
      val op = createMockOperation
      testFit(op, transformedDataFrame1, transformer1)
      op.set(op.estimator.paramA -> 2)
      testFit(op, transformedDataFrame2, transformer2)
    }
    "infer types using properly set parameters" in {
      def testInference(
          op: MockEstimatorOperation,
          expectedSchema: StructType,
          expectedTransformerKnowledge: DKnowledge[Transformer]): Unit = {

        val inputDF = DataFrame.forInference(mock[StructType])
        val (knowledge, warnings) =
          op.inferKnowledge(mock[InferContext])(Vector(DKnowledge(inputDF)))
        // Warnings should be a sum of transformer inference warnings
        // and estimator inference warnings. Currently, either both of them
        // are empty or the inferences throw exception, so the sum is always 'empty'.
        warnings shouldBe InferenceWarnings.empty
        val Vector(dataFrameKnowledge, transformerKnowledge) = knowledge
        dataFrameKnowledge shouldBe DKnowledge(DataFrame.forInference(expectedSchema))
        transformerKnowledge shouldBe expectedTransformerKnowledge
      }

      val op = createMockOperation
      testInference(op, transformedDataFrameSchema1, transformerKnowledge1)
      op.set(op.estimator.paramA -> 2)
      testInference(op, transformedDataFrameSchema2, transformerKnowledge2)
    }
  }
}

object EstimatorAsOperationSpec extends UnitSpec {
  class MockEstimatorOperation
    extends EstimatorAsOperation[MockEstimator, Transformer] {
    override val id: Id = Id.randomId
    override val name: String = "Mock Estimator as an Operation"
    override val description: String = "Description"
  }
}

