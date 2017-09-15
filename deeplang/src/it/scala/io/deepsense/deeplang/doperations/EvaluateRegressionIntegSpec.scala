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

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}

import io.deepsense.commons.types.ColumnType
import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.Report
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.exceptions.{ColumnDoesNotExistException, WrongColumnTypeException}
import io.deepsense.reportlib.model.Table

class EvaluateRegressionIntegSpec extends DeeplangIntegTestSupport {

  val nameColumnName = "name"
  val targetColumnName = "target"
  val predictionColumnName = "prediction"
  val schemaSeq = Seq(
    StructField(nameColumnName, StringType),
    StructField(targetColumnName, DoubleType),
    StructField(predictionColumnName, DoubleType))
  val schema = StructType(schemaSeq)
  val correctRows = Seq(
    Row("a", 1.0, 1.0),
    Row("b", 2.0, 2.0),
    Row("c", 3.0, 3.0),
    Row("d", 1.0, 1.0),
    Row("e", 2.0, 2.0),
    Row("f", 3.0, 3.0),
    Row("g", 1.0, 1.0),
    Row("h", 2.0, 2.0),
    Row("i", 3.0, 3.0),
    Row("j", 1.0, 1.0),
    Row("k", 2.0, 2.0),
    Row("l", 3.0, 3.0),
    Row("m", 1.0, 1.0),
    Row("n", 2.0, 2.0),
    Row("o", 3.0, 3.0),
    Row("p", 1.0, 1.0),
    Row("r", 2.0, 2.0),
    Row("s", 3.0, 3.0),
    Row("t", 1.0, 1.0),
    Row("u", 2.0, 2.0),
    Row("w", 3.0, 3.0),
    Row("x", 1.0, 1.0),
    Row("y", 2.0, 2.0),
    Row("z", 3.0, 3.0))
  val wrongPredictionsRows = correctRows.map(r => {
    val rowSeq: Seq[Any] = r.toSeq
    Row.fromSeq(rowSeq.dropRight(1) :+ rowSeq.last.asInstanceOf[Double] + 0.1)
  })

  "EvaluateRegression" should {
    def testDataFrame = createDataFrame(correctRows, schema)
    "throw exception" when {
      "selected target column is not Double" in {
        intercept[WrongColumnTypeException] {
          evaluateRegression(testDataFrame, nameColumnName, predictionColumnName)
        }
      }
      "prediction column is not Double" in {
        intercept[WrongColumnTypeException] {
          evaluateRegression(testDataFrame, targetColumnName, nameColumnName)
        }
      }
      "target column does not exist" in {
        intercept[ColumnDoesNotExistException] {
          evaluateRegression(testDataFrame, "whatever", predictionColumnName)
        }
      }
      "prediction column does not exist" in {
        intercept[ColumnDoesNotExistException] {
          evaluateRegression(testDataFrame, targetColumnName, "blah")
        }
      }
    }
    "return report for all correct predictions" in {
      val report = evaluateRegression(testDataFrame, targetColumnName, predictionColumnName)
      assertEvaluateRegressionReport(
        report,
        List(List(Some("24"), Some("1"), Some("0"), Some("0"), Some("1"), Some("0"))))
    }
    "return report for not entirely correct predictions" in {
      val dataFrameWithWrongPredictions = createDataFrame(wrongPredictionsRows, schema)
      val report = evaluateRegression(
        dataFrameWithWrongPredictions,
        targetColumnName,
        predictionColumnName)
      assertEvaluateRegressionReport(
        report,
        List(List(Some("24"), Some("1"), Some("0.1"), Some("0.01"), Some("0.985"), Some("0.1"))))
    }
  }

  private def assertEvaluateRegressionReport(
      report: Report,
      values: List[List[Some[String]]]): Registration = {
    val tableName: String = "Evaluate Regression Report"
    report.content.distributions shouldBe empty
    report.content.tables.size shouldBe 1
    report.content.tables(tableName) shouldBe
      Table(
        tableName,
        "Evaluate regression metrics",
        Some(
          List(
            "DataFrame Size",
            "Explained Variance",
            "Mean Absolute Error",
            "Mean Squared Error",
            "r2",
            "Root Mean Squared Error")),
        Some(
          List(
            ColumnType.numeric,
            ColumnType.numeric,
            ColumnType.numeric,
            ColumnType.numeric,
            ColumnType.numeric,
            ColumnType.numeric
          )),
        None,
        values
    )
  }

  private def evaluateRegression(
      dataFrame: DataFrame,
      targetColumnName: String,
      predictionColumnName: String): Report = {
    val operation = EvaluateRegression(targetColumnName, predictionColumnName)
    val resultVector = operation.execute(executionContext)(Vector(dataFrame))
    resultVector.head.asInstanceOf[Report]
  }

}
