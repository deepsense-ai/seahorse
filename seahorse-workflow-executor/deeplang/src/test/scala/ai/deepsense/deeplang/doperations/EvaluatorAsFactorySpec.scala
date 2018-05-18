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

import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang.DOperation._
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.report.Report
import ai.deepsense.deeplang.doperables.{Evaluator, MetricValue}
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import ai.deepsense.deeplang.params.{NumericParam, Param, ParamMap}
import ai.deepsense.deeplang.{DKnowledge, ExecutionContext, ReportTypeDefault, UnitSpec}

class EvaluatorAsFactorySpec extends UnitSpec {
  import EvaluatorAsFactorySpec._

  "EvaluatorAsFactory" should {
    "have the same parameters as the Evaluator" in {
      val mockEvaluator = new MockEvaluator
      val mockFactory = new MockEvaluatorFactory
      val reportType = ReportTypeDefault(mockFactory.reportType)
      mockFactory.extractParamMap() shouldBe mockEvaluator.extractParamMap() ++ ParamMap(reportType)
      mockFactory.specificParams shouldBe mockEvaluator.params
    }

    val paramValue1 = 100
    val paramValue2 = 1337

    "produce an Evaluator with parameters set" in {
      val mockFactory = new MockEvaluatorFactory
      mockFactory.set(mockFactory.evaluator.param -> paramValue1)
      val Vector(evaluator: MockEvaluator) =
        mockFactory.executeUntyped(Vector.empty)(mock[ExecutionContext])

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

      val (Vector(knowledge), warnings) = mockFactory.inferKnowledgeUntyped(Vector.empty)(mock[InferContext])

      knowledge should have size 1
      knowledge.single shouldBe a[MockEvaluator]
      val evaluator = knowledge.single.asInstanceOf[MockEvaluator]
      evaluator.extractParamMap() shouldBe execute(mockFactory).extractParamMap()

      warnings shouldBe InferenceWarnings.empty
    }
  }

  private def execute(factory: MockEvaluatorFactory): MockEvaluator =
    factory.executeUntyped(Vector.empty)(mock[ExecutionContext]).head.asInstanceOf[MockEvaluator]
}

object EvaluatorAsFactorySpec {

  class MockEvaluator extends Evaluator {
    val param = NumericParam("b", Some("desc"))
    setDefault(param -> 5)
    override val params: Array[Param[_]] = Array(param)

    override private[deeplang] def _evaluate(ctx: ExecutionContext, df: DataFrame): MetricValue =
      ???
    override private[deeplang] def _infer(k: DKnowledge[DataFrame]): MetricValue =
      ???
    override def report(extended: Boolean = true): Report =
      ???

    override def isLargerBetter: Boolean = ???
  }

  class MockEvaluatorFactory extends EvaluatorAsFactory[MockEvaluator] {
    override val id: Id = Id.randomId
    override val name: String = "Mock Evaluator factory used for tests purposes"
    override val description: String = "Description"
  }
}
