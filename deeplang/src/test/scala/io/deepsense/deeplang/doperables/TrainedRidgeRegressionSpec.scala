/**
 * Copyright 2015, CodiLime Inc.
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

package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.regression.RidgeRegressionModel
import org.apache.spark.mllib.linalg

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.reportlib.model.{Table, ReportContent}

class TrainedRidgeRegressionSpec extends ScorableSpec[TrainedRidgeRegression]{
  def scorableName: String = "TrainedRidgeRegression"

  def scorable: Scorable = new TrainedRidgeRegression()

  "TrainedRidgeRegression" should {
    "generate report" in {
      val executionContext = new ExecutionContext(mock[DOperableCatalog])

      val weights = linalg.Vectors.dense(0.4, 10.3, -2.7)
      val intercept = -3.14
      val model = new RidgeRegressionModel(weights, intercept)
      val featureColumns = Seq("abc", "def", "ghi")
      val targetColumn = "xyz"

      val regression = TrainedRidgeRegression(
        Some(model), Some(featureColumns), Some(targetColumn), None)

      regression.report(executionContext) shouldBe Report(ReportContent(
        "Report for TrainedRidgeRegression",
        tables = Map(
          "Model weights" -> Table(
            "Model weights", "", Some(List("Column", "Weight")), None,
            values = List(
              List(Some(""), Some(intercept.toString)),
              List(Some(featureColumns(0)), Some(weights(0).toString)),
              List(Some(featureColumns(1)), Some(weights(1).toString)),
              List(Some(featureColumns(2)), Some(weights(2).toString))

            )
          ),
          "Target column" -> Table(
            "Target column", "", None, None, List(List(Some(targetColumn)))
          )
        )
      ))
    }
  }
}
