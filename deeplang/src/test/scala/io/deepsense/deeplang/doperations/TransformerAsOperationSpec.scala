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

import scala.reflect.runtime.universe.TypeTag

import org.apache.spark.sql.types.StructType

import io.deepsense.commons.utils.Version
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.Transformer
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.report.Report
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.params.validators.RangeValidator
import io.deepsense.deeplang.params.{NumericParam, Param, ParamMap}
import io.deepsense.deeplang.{DKnowledge, DeeplangTestSupport, ExecutionContext, UnitSpec}

object MockTransformers extends UnitSpec with DeeplangTestSupport {
  val DefaultForA = 1

  val inputDataFrame = createDataFrame()
  val outputDataFrame1 = createDataFrame()
  val outputDataFrame2 = createDataFrame()

  val inputSchema = inputDataFrame.schema.get
  val outputSchema1 = outputDataFrame1.schema.get
  val outputSchema2 = outputDataFrame2.schema.get

  class MockTransformer extends Transformer {
    val paramA = NumericParam("a", "desc", RangeValidator(0.0, Double.MaxValue))
    setDefault(paramA -> DefaultForA)

    override val params: Array[Param[_]] = declareParams(paramA)

    override def report: Report = ???

    private[deeplang] def _transform(ctx: ExecutionContext, df: DataFrame): DataFrame = {
      $(paramA) match {
        case 1 => outputDataFrame1
        case -2 | 2 => outputDataFrame2
      }
    }

    override private[deeplang] def _transformSchema(schema: StructType): Option[StructType] = {
      Some($(paramA) match {
        case 1 => outputSchema1
        case -2 | 2 => outputSchema2
      })
    }

    override def load(ctx: ExecutionContext, path: String): this.type = ???

    override protected def saveTransformer(ctx: ExecutionContext, path: String): Unit = ???
  }
}

class TransformerAsOperationSpec extends UnitSpec {
  import MockTransformers._

  class MockTransformerAsOperation extends TransformerAsOperation[MockTransformer] {
    override val name: String = ""
    override val id: Id = "6d924962-9456-11e5-8994-feff819cdc9f"
    override val description: String = ""
  }

  "TransformerAsOperation" should {
    def operation: MockTransformerAsOperation = new MockTransformerAsOperation

    "have params same as Transformer" in {
      val op = operation
      op.params shouldBe Array(op.transformer.paramA)
    }
    "have defaults same as in Transformer" in {
      val op = operation
      op.extractParamMap() shouldBe ParamMap(op.transformer.paramA -> DefaultForA)
    }
    "execute transform using transformer with properly set params and return it" in {
      val op = operation
      op.set(op.transformer.paramA -> 2)
      val result = op.executeUntyped(Vector(mock[DataFrame]))(mock[ExecutionContext])

      result should have length 2
      result(0).asInstanceOf[DataFrame] shouldBe outputDataFrame2
      result(1).asInstanceOf[MockTransformer].extractParamMap() shouldBe
        ParamMap(op.transformer.paramA -> 2)
    }
    "infer types on transformer with properly set params and return it" in {
      val op = operation
      op.set(op.transformer.paramA -> 2)

      val inputDF = inputDataFrame
      val (result, warnings) =
        op.inferKnowledgeUntyped(Vector(DKnowledge(inputDF)))(mock[InferContext])

      warnings shouldBe InferenceWarnings.empty

      result should have length 2
      result(0).asInstanceOf[DKnowledge[DataFrame]] shouldBe
        DKnowledge(DataFrame.forInference(outputSchema2))
      result(1).single.asInstanceOf[MockTransformer].extractParamMap() shouldBe
        ParamMap(op.transformer.paramA -> 2)
    }
  }
}
