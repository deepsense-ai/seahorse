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

package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.linalg.{Vector => LinAlgVector, Vectors}

import io.deepsense.commons.types.ColumnType
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.doperables.machinelearning.LinearRegressionParameters
import io.deepsense.deeplang.doperables.machinelearning.ridgeregression.TrainedRidgeRegression
import io.deepsense.deeplang.{DOperable, ExecutionContext}
import io.deepsense.reportlib.model.{ReportContent, Table}

abstract class BaseTrainedLinearRegressionSpec extends ScorableSpec[TrainedRidgeRegression] {

  def scorableName: String

  def scorable: Scorable

  def createRegression(
      params: LinearRegressionParameters,
      featureColumns: Seq[String],
      targetColumn: String,
      weights: LinAlgVector,
      intercept: Double): DOperable

  scorableName should {
    "generate report" in {
      val executionContext = new ExecutionContext(mock[DOperableCatalog])

      val weights = Vectors.dense(0.4, 10.3, -2.7)
      val intercept = -3.14
      val featureColumns = Seq("abc", "def", "ghi")
      val targetColumn = "xyz"
      val params = LinearRegressionParameters(0.1, 11, 0.9)

      val regression = createRegression(params, featureColumns, targetColumn, weights, intercept)

      regression.report(executionContext) shouldBe Report(ReportContent(
        "Report for " + scorableName,
        tables = Map(
          "Model weights" -> Table(
            "Model weights", "",
            Some(List("Column", "Weight")),
            List(ColumnType.string, ColumnType.numeric),
            None,
            values = List(
              List(Some(featureColumns(0)), Some(weights(0).toString)),
              List(Some(featureColumns(1)), Some(weights(1).toString)),
              List(Some(featureColumns(2)), Some(weights(2).toString))
            )
          ),
          "Intercept" -> Table(
            "Intercept", "",
            None,
            List(ColumnType.numeric),
            None,
            List(List(Some(intercept.toString)))
          ),
          "Parameters" -> Table(
            "Parameters", "",
            Some(List(
              "Regularization parameter",
              "Iterations number",
              "Mini batch fraction")),
            List(ColumnType.numeric, ColumnType.numeric, ColumnType.numeric),
            None,
            List(
              List(
                params.regularizationParameter, params.numberOfIterations, params.miniBatchFraction
              ).map(value => Some(value.toString)))
          ),
          "Target column" -> Table(
            "Target column", "",
            Some(List("Target column")),
            List(ColumnType.string),
            None,
            List(List(Some(targetColumn)))
          ),
          "Feature columns" -> Table(
            "Feature columns", "",
            Some(List("Feature columns")),
            List(ColumnType.string),
            None,
            featureColumns.map(value => List(Some(value))).toList
          )
        )
      ))
    }
  }
}
