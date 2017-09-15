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

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.{Report, Transformer}
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.params.{NumericParam, Param, ParamMap}
import io.deepsense.deeplang.{DKnowledge, ExecutionContext, UnitSpec}

object MockTransformers extends UnitSpec {
  val DefaultForA = 1

  val outputDataFrame1 = mock[DataFrame]
  val outputDataFrame2 = mock[DataFrame]

  val outputSchema1 = mock[StructType]
  val outputSchema2 = mock[StructType]

  class MockTransformer extends Transformer {
    val paramA = NumericParam("a", "desc")
    setDefault(paramA -> DefaultForA)

    override val params: Array[Param[_]] = declareParams(paramA)

    override def report(executionContext: ExecutionContext): Report = ???

    private[deeplang] def _transform(ctx: ExecutionContext, df: DataFrame): DataFrame = {
      $(paramA) match {
        case 1 => outputDataFrame1
        case 2 => outputDataFrame2
      }
    }

    override private[deeplang] def _transformSchema(schema: StructType): Option[StructType] = {
      Some($(paramA) match {
        case 1 => outputSchema1
        case 2 => outputSchema2
      })
    }
  }
}

class TransformerAsOperationSpec extends UnitSpec {
  import MockTransformers._

  class MockTransformerAsOperation extends TransformerAsOperation[MockTransformer] {

    override val tTagTO_1: TypeTag[MockTransformer] = typeTag[MockTransformer]
    override val name: String = ""
    override val id: Id = "6d924962-9456-11e5-8994-feff819cdc9f"
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
      val result = op.execute(mock[ExecutionContext])(Vector(mock[DataFrame]))

      result should have length 2
      result(0).asInstanceOf[DataFrame] shouldBe outputDataFrame2
      result(1).asInstanceOf[MockTransformer].extractParamMap() shouldBe
        ParamMap(op.transformer.paramA -> 2)
    }
    "infer types on transformer with properly set params and return it" in {
      val op = operation
      op.set(op.transformer.paramA -> 2)

      val inputDF = DataFrame.forInference(mock[StructType])
      val (result, warnings) =
        op.inferKnowledge(mock[InferContext])(Vector(DKnowledge(inputDF)))

      warnings shouldBe InferenceWarnings.empty

      result should have length 2
      result(0).asInstanceOf[DKnowledge[DataFrame]] shouldBe
        DKnowledge(DataFrame.forInference(outputSchema2))
      result(1).single.asInstanceOf[MockTransformer].extractParamMap() shouldBe
        ParamMap(op.transformer.paramA -> 2)
    }
  }
}
