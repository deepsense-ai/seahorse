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

import io.deepsense.deeplang.DOperation._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.{Evaluator, MetricValue, Report}
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.params.{NumericParam, Param}
import io.deepsense.deeplang.{DKnowledge, ExecutionContext, UnitSpec}

class EvaluatorAsFactorySpec extends UnitSpec {
  import EvaluatorAsFactorySpec._

  "EvaluatorAsFactory" should {
    "have the same parameters as the Evaluator" in {
      val mockEvaluator = new MockEvaluator
      val mockFactory = new MockEvaluatorFactory
      mockFactory.extractParamMap() shouldBe mockEvaluator.extractParamMap()
      mockFactory.params shouldBe mockEvaluator.params
    }

    val paramValue1 = 100
    val paramValue2 = 1337

    "produce an Evaluator with parameters set" in {
      val mockFactory = new MockEvaluatorFactory
      mockFactory.set(mockFactory.evaluator.param -> paramValue1)
      val Vector(evaluator: MockEvaluator) =
        mockFactory.execute(mock[ExecutionContext])(Vector.empty)

      evaluator.get(mockFactory.evaluator.param) shouldBe Some(paramValue1)
    }

    "propagate parameters to wrapped evaluator" in {
      val mockFactory = new MockEvaluatorFactory
      mockFactory.set(mockFactory.evaluator.param -> paramValue1)
      val evaluator1 = execute(mockFactory)
      evaluator1.get(mockFactory.evaluator.param) shouldBe Some(paramValue1)

      mockFactory.set(mockFactory.evaluator.param -> paramValue2)
      val evaluator2 = execute(mockFactory)
      evaluator2.get(mockFactory.evaluator.param) shouldBe Some(paramValue2)
    }

    "infer knowledge" in {
      val mockFactory = new MockEvaluatorFactory
      mockFactory.set(mockFactory.evaluator.param -> paramValue1)

      val (Vector(knowledge), warnings) =
        mockFactory.inferKnowledge(mock[InferContext])(Vector.empty)

      knowledge should have size 1
      knowledge.single shouldBe a[MockEvaluator]
      val evaluator = knowledge.single.asInstanceOf[MockEvaluator]
      evaluator.extractParamMap() shouldBe execute(mockFactory).extractParamMap()

      warnings shouldBe InferenceWarnings.empty
    }
  }

  private def execute(factory: MockEvaluatorFactory): MockEvaluator =
    factory.execute(mock[ExecutionContext])(Vector.empty).head.asInstanceOf[MockEvaluator]
}

object EvaluatorAsFactorySpec {

  class MockEvaluator extends Evaluator {
    val param = NumericParam("b", "desc")
    setDefault(param -> 5)
    override val params: Array[Param[_]] = declareParams(param)

    override private[deeplang] def _evaluate(ctx: ExecutionContext, df: DataFrame): MetricValue =
      ???
    override private[deeplang] def _infer(k: DKnowledge[DataFrame]): MetricValue =
      ???
    override def report: Report =
      ???

    override def isLargerBetter: Boolean = ???
  }

  class MockEvaluatorFactory extends EvaluatorAsFactory[MockEvaluator] {
    override val id: Id = Id.randomId
    override val name: String = "Mock Evaluator factory used for tests purposes"
    override val description: String = "Description"
  }
}
