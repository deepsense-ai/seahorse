/**
 * Copyright 2016, deepsense.io
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
import spray.json.{JsNumber, JsObject}

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.report.Report
import io.deepsense.deeplang.doperations.MockDOperablesFactory.{MockEstimator, MockEvaluator}
import io.deepsense.deeplang.exceptions.DeepLangMultiException
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.{DKnowledge, UnitSpec}

class GridSearchSpec extends UnitSpec {
  "GridSearch" should {
    "infer knowledge when dynamic parameters are valid" in {
      val inputDF = DataFrame.forInference(mock[StructType])
      val estimator = new MockEstimator
      val evaluator = new MockEvaluator

      val gridSearch = GridSearch()
      gridSearch.inferKnowledge(mock[InferContext])(
          Vector(DKnowledge(inputDF), DKnowledge(estimator), DKnowledge(evaluator))) shouldBe
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

    val inputDF = DataFrame.forInference(mock[StructType])
    val estimator = new MockEstimator
    val evaluator = new MockEvaluator

    val gridSearch = GridSearch()
      .setEstimatorParams(prepareParamDictionary(estimator.paramA.name, estimatorParamValue))
      .setEvaluatorParams(prepareParamDictionary(evaluator.paramA.name, evaluatorParamValue))

    val multiException = the [DeepLangMultiException] thrownBy {
      gridSearch.inferKnowledge(mock[InferContext])(
        Vector(
          DKnowledge(inputDF),
          DKnowledge(estimator),
          DKnowledge(evaluator)))
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
