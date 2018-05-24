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

import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import ai.deepsense.deeplang.params.ParamMap
import ai.deepsense.deeplang._

class TransformerAsFactorySpec extends UnitSpec {
  import MockTransformers._

  class MockTransformerAsFactory extends TransformerAsFactory[MockTransformer] {
    override val name: String = ""
    override val id: Id = "6d924962-9456-11e5-8994-feff819cdc9f"
    override val description: String = ""
  }

  "TransformerAsFactory" should {
    def operation: MockTransformerAsFactory = new MockTransformerAsFactory

    "have params same as Transformer" in {
      val op = operation
      op.specificParams shouldBe Array(op.transformer.paramA)
    }
    "have report type param set to extended" in {
      val op = operation
      op.extractParamMap().get(op.reportType).get shouldBe DOperation.ReportParam.Extended()
    }
    "have defaults same as in Transformer" in {
      val op = operation
      val transformerParam = op.transformer.paramA -> DefaultForA
      op.extractParamMap() shouldBe ParamMap(transformerParam, ReportTypeDefault(op.reportType))
    }
    "produce transformer with properly set params" in {
      val op = operation
      op.set(op.transformer.paramA -> 2)
      val result = op.executeUntyped(Vector())(mock[ExecutionContext])

      result should have length 1
      result(0).asInstanceOf[MockTransformer].extractParamMap() shouldBe
        ParamMap(op.transformer.paramA -> 2, ReportTypeDefault(op.reportType))
    }
    "infer knowledge" in {
      val op = operation
      op.set(op.transformer.paramA -> 2)

      val (result, warnings) =
        op.inferKnowledgeUntyped(Vector(DKnowledge()))(mock[InferContext])

      warnings shouldBe InferenceWarnings.empty

      result should have length 1
      result(0).single.asInstanceOf[MockTransformer].extractParamMap() shouldBe
        ParamMap(op.transformer.paramA -> 2, ReportTypeDefault(op.reportType))
    }
  }
}
