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

import scala.collection.mutable
import scala.reflect.runtime.universe.TypeTag

import org.apache.spark.sql.types.StructType
import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang.doperables.Transformer
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.report.Report
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import ai.deepsense.deeplang.params.validators.RangeValidator
import ai.deepsense.deeplang.params.{NumericParam, Param, ParamMap, ParamPair}
import ai.deepsense.deeplang._

object MockTransformers extends UnitSpec with DeeplangTestSupport {
  val DefaultForA = 1

  val inputDataFrame = createDataFrame()
  val outputDataFrame1 = createDataFrame()
  val outputDataFrame2 = createDataFrame()

  val inputSchema = inputDataFrame.schema.get
  val outputSchema1 = outputDataFrame1.schema.get
  val outputSchema2 = outputDataFrame2.schema.get

  class MockTransformer extends Transformer {
    val paramA = NumericParam("a", Some("desc"), RangeValidator(0.0, Double.MaxValue))
    setDefault(paramA -> DefaultForA)

    override val params: Array[Param[_]] = Array(paramA)

    override def report(extended: Boolean = true): Report = ???

    override protected def applyTransform(ctx: ExecutionContext, df: DataFrame): DataFrame = {
      $(paramA) match {
        case 1 => outputDataFrame1
        case -2 | 2 => outputDataFrame2
      }
    }

    override protected def applyTransformSchema(schema: StructType): Option[StructType] = {
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

    override val tTagTO_1: TypeTag[MockTransformer] = typeTag[MockTransformer]
    override val name: String = ""
    override val id: Id = "6d924962-9456-11e5-8994-feff819cdc9f"
    override val description: String = ""
  }

  "TransformerAsOperation" should {
    def operation: MockTransformerAsOperation = new MockTransformerAsOperation
    val op = operation
    val changedParamMap =
      ParamMap(op.transformer.paramA -> 2, op.reportType -> DOperation.ReportParam.Extended())
    "have specific params same as Transformer" in {
      op.specificParams shouldBe Array(op.transformer.paramA)
    }

    "have report type param set to extended" in {
      op.extractParamMap().get(op.reportType).get shouldBe DOperation.ReportParam.Extended()
    }

    "have defaults same as in Transformer" in {
      op.extractParamMap() shouldBe ParamMap(op.transformer.paramA -> DefaultForA, ReportTypeDefault(op.reportType))
    }
    "execute transform using transformer with properly set params and return it" in {
      op.set(op.transformer.paramA -> 2)
      val result = op.executeUntyped(Vector(mock[DataFrame]))(mock[ExecutionContext])

      result should have length 2
      result(0).asInstanceOf[DataFrame] shouldBe outputDataFrame2
      result(1).asInstanceOf[MockTransformer].extractParamMap() shouldBe changedParamMap

    }
    "infer types on transformer with properly set params and return it" in {
      op.set(op.transformer.paramA -> 2)

      val inputDF = inputDataFrame
      val (result, warnings) =
        op.inferKnowledgeUntyped(Vector(DKnowledge(inputDF)))(mock[InferContext])

      warnings shouldBe InferenceWarnings.empty

      result should have length 2
      result(0).asInstanceOf[DKnowledge[DataFrame]] shouldBe
        DKnowledge(DataFrame.forInference(outputSchema2))
      result(1).single.asInstanceOf[MockTransformer].extractParamMap() shouldBe changedParamMap
    }
  }
}
