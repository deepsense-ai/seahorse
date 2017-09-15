/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

import spray.json.{JsNumber, JsObject}

import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.report.Report
import ai.deepsense.deeplang.doperations.MockDOperablesFactory.{MockEstimator, MockEvaluator}
import ai.deepsense.deeplang.exceptions.DeepLangMultiException
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import ai.deepsense.deeplang.{DKnowledge, DeeplangTestSupport, UnitSpec}

class GridSearchSpec extends UnitSpec with DeeplangTestSupport {
  "GridSearch" should {
    "infer knowledge when dynamic parameters are valid" in {
      val inputDF = DataFrame.forInference(createSchema())
      val estimator = new MockEstimator
      val evaluator = new MockEvaluator

      val gridSearch = GridSearch()
      gridSearch.inferKnowledgeUntyped(
          Vector(DKnowledge(estimator), DKnowledge(inputDF), DKnowledge(evaluator)))(mock[InferContext]) shouldBe
        (Vector(DKnowledge(Report())), InferenceWarnings.empty)
    }
    "throw Exception" when {
      "Estimator's dynamic parameters are invalid" in {
        checkMultiException(Some(-2), None)
      }
      "Evaluator's dynamic parameters are invalid" in {
        checkMultiException(None, Some(-2))
      }
      "Both Estimator's and Evaluator's dynamic parameters are invalid" in {
        checkMultiException(Some(-2), Some(-2))
      }
    }
  }

  private def checkMultiException(
      estimatorParamValue: Option[Double],
      evaluatorParamValue: Option[Double]): Unit = {

    val inputDF = DataFrame.forInference(createSchema())
    val estimator = new MockEstimator
    val evaluator = new MockEvaluator

    val gridSearch = GridSearch()
      .setEstimatorParams(prepareParamDictionary(estimator.paramA.name, estimatorParamValue))
      .setEvaluatorParams(prepareParamDictionary(evaluator.paramA.name, evaluatorParamValue))

    val multiException = the [DeepLangMultiException] thrownBy {
      gridSearch.inferKnowledgeUntyped(
        Vector(
          DKnowledge(estimator),
          DKnowledge(inputDF),
          DKnowledge(evaluator)))(mock[InferContext])
    }

    val invalidParamCount =
      estimatorParamValue.map(_ => 1).getOrElse(0) +
      evaluatorParamValue.map(_ => 1).getOrElse(0)

    multiException.exceptions should have size invalidParamCount
  }

  private def prepareParamDictionary(paramName: String, maybeValue: Option[Double]): JsObject = {
    val jsonEntries = maybeValue.map(
        value => Seq(paramName -> JsNumber(value)))
      .getOrElse(Seq())
    JsObject(jsonEntries: _*)
  }
}
