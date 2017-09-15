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
import org.mockito.Matchers._
import org.mockito.Mockito._

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.params.{NumericParam, Param, ParamMap}


class EstimatorAsOperationIntegSpec extends UnitSpec {
  import EstimatorAsOperationIntegSpec._

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

object EstimatorAsOperationIntegSpec extends UnitSpec {
  val DefaultForA = 1

  def mockTransformer(df: DataFrame, dfk: DKnowledge[DataFrame]): Transformer = {
    val t = mock[Transformer]
    val transform: DMethod1To1[Unit, DataFrame, DataFrame] =
      mock[DMethod1To1[Unit, DataFrame, DataFrame]]
    when(transform.apply(any())(any())(any())).thenReturn(df)
    when(transform.infer(any())(any())(any())).thenReturn((dfk, InferenceWarnings.empty))
    when(t.transform).thenReturn(transform)
    t
  }

  def dataFrameKnowledge(s: StructType): DKnowledge[DataFrame] = {
    val k = mock[DKnowledge[DataFrame]]
    when(k.types).thenReturn(Set(DataFrame.forInference(s)))
    k
  }

  def transformerKnowledge(t: Transformer): DKnowledge[Transformer] = {
    val knowledge: DKnowledge[Transformer] = mock[DKnowledge[Transformer]]
    when(knowledge.single).thenReturn(t)
    knowledge
  }

  val transformedDataFrame1 = mock[DataFrame]
  val transformedDataFrame2 = mock[DataFrame]
  val transformedDataFrameSchema1 = mock[StructType]
  val transformedDataFrameSchema2 = mock[StructType]
  val transformedDataFrameKnowledge1 = dataFrameKnowledge(transformedDataFrameSchema1)
  val transformedDataFrameKnowledge2 = dataFrameKnowledge(transformedDataFrameSchema2)
  val transformer1 = mockTransformer(transformedDataFrame1, transformedDataFrameKnowledge1)
  val transformer2 = mockTransformer(transformedDataFrame2, transformedDataFrameKnowledge2)
  val transformerKnowledge1 = transformerKnowledge(transformer1)
  val transformerKnowledge2 = transformerKnowledge(transformer2)

  class MockEstimator extends Estimator {
    val paramA = NumericParam("b", "desc")
    setDefault(paramA -> DefaultForA)
    override val params: Array[Param[_]] = declareParams(paramA)
    override def report(executionContext: ExecutionContext): Report = ???

    override def fit: DMethod1To1[Unit, DataFrame, Transformer] = {
      new DMethod1To1[Unit, DataFrame, Transformer] {
        override def apply(context: ExecutionContext)(parameters: Unit)(t0: DataFrame)
          : Transformer = {
          $(paramA) match {
            case 1 => transformer1
            case 2 => transformer2
          }
        }

        override def infer
          (context: InferContext)
            (parameters: Unit)
            (k0: DKnowledge[DataFrame])
          : (DKnowledge[Transformer], InferenceWarnings) = {
          $(paramA) match {
            case 1 => (transformerKnowledge1, InferenceWarnings.empty)
            case 2 => (transformerKnowledge2, InferenceWarnings.empty)
          }
        }
      }
    }

    // Not used in tests.
    override private[deeplang] def _fit(df: DataFrame): Transformer = ???
    override private[deeplang] def _fit_infer(schema: Option[StructType]): Transformer = ???
  }

  class MockEstimatorOperation
    extends EstimatorAsOperation[MockEstimator] {
    override val id: Id = Id.randomId
    override val name: String = "Mock Estimator as an Operation"
  }
}

