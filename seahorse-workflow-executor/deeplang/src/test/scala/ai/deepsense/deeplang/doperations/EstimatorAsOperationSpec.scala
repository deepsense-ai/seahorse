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

import org.apache.spark.sql.types.StructType
import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang._
import ai.deepsense.deeplang.doperables._
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperations.MockDOperablesFactory._
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import ai.deepsense.deeplang.params.ParamMap

class EstimatorAsOperationSpec extends UnitSpec with DeeplangTestSupport {
  import EstimatorAsOperationSpec._

  "EstimatorAsOperation" should {
    def createMockOperation: MockEstimatorOperation = new MockEstimatorOperation
    "have the same specific parameters as the Estimator" in {
      val op = createMockOperation
      op.specificParams shouldBe op.estimator.params
    }
   "have the same default values for parameters as Estimator" in {
     val op = createMockOperation
     val estimatorOperation = op.estimator.paramA -> DefaultForA
     op.extractParamMap() shouldBe ParamMap(estimatorOperation, ReportTypeDefault(op.reportType))
    }
    "execute fit using properly set parameters" in {
      def testFit(
          op: MockEstimatorOperation,
          expectedDataFrame: DataFrame,
          expectedTransformer: Transformer): Unit = {
        val Vector(outputDataFrame: DataFrame, outputTransformer: Transformer) =
          op.executeUntyped(Vector(mock[DataFrame]))(mock[ExecutionContext])
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

        val inputDF = DataFrame.forInference(createSchema())
        val (knowledge, warnings) = op.inferKnowledgeUntyped(Vector(DKnowledge(inputDF)))(mock[InferContext])
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

