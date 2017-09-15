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
import org.apache.spark.sql.types._

import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.Report
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.exceptions.{ColumnDoesNotExistException, WrongColumnTypeException}
import io.deepsense.reportlib.model.{ReportContent, Table}

class EvaluateClassificationIntegSpec extends DeeplangIntegTestSupport {

  val nameColumnName = "name"
  val targetDoubleColumnName = "targetDouble"
  val targetBooleanColumnName = "targetBoolean"
  val target2CategoricalColumnName = "target2Categorical"
  val predictionColumnName = "classificationPrediction"
  val schemaSeq = Seq(
    StructField(nameColumnName, StringType),
    StructField(predictionColumnName, DoubleType),
    StructField(targetDoubleColumnName, DoubleType),
    StructField(targetBooleanColumnName, BooleanType),
    StructField(target2CategoricalColumnName, StringType)
  )
  val schema = StructType(schemaSeq)
  val correctRows = Seq(
    Row("a", 0.99, 1.0, true, "Y"),
    Row("b", 0.99, 1.0, true, "Y"),
    Row("c", 0.99, 1.0, true, "Y"),
    Row("d", 0.01, 0.0, false, "N"),
    Row("e", 0.99, 1.0, true, "Y"),
    Row("f", 0.99, 1.0, true, "Y"),
    Row("g", 0.01, 0.0, false, "N"),
    Row("h", 0.01, 0.0, false, "N"),
    Row("i", 0.01, 0.0, false, "N"),
    Row("j", 0.99, 1.0, true, "Y"),
    Row("k", 0.99, 1.0, true, "Y"),
    Row("l", 0.99, 1.0, true, "Y"),
    Row("m", 0.01, 0.0, false, "N"),
    Row("n", 0.01, 0.0, false, "N"),
    Row("o", 0.01, 0.0, false, "N"),
    Row("p", 0.01, 0.0, false, "N"),
    Row("r", 0.01, 0.0, false, "N"),
    Row("s", 0.01, 0.0, false, "N"),
    Row("t", 0.01, 0.0, false, "N"),
    Row("u", 0.01, 0.0, false, "N"),
    Row("w", 0.99, 1.0, true, "Y"),
    Row("x", 0.99, 1.0, true, "Y"),
    Row("y", 0.99, 1.0, true, "Y"),
    Row("z", 0.99, 1.0, true, "Y"))

  "EvaluateClassification" should {
    def testDataFrame: DataFrame = createDataFrame(correctRows, schema)
    "throw exception" when {
      "selected target column is String" in {
        a[WrongColumnTypeException] should be thrownBy {
          executeEvaluation(testDataFrame, nameColumnName, predictionColumnName)
        }
      }
      "classification prediction column is String" in {
        a[WrongColumnTypeException] should be thrownBy {
          executeEvaluation(testDataFrame, targetDoubleColumnName, nameColumnName)
        }
      }
      "target column does not exist" in {
        a[ColumnDoesNotExistException] should be thrownBy {
          executeEvaluation(testDataFrame, "whatever", predictionColumnName)
        }
      }
      "classification prediction column does not exist" in {
        a[ColumnDoesNotExistException] should be thrownBy {
          executeEvaluation(testDataFrame, targetDoubleColumnName, "blah")
        }
      }
      "target column is a categorical with more than 2 values" in {
        a[WrongColumnTypeException] should be thrownBy {
          val dataFrame: DataFrame =
            createDataFrame(correctRows, schema, Seq(nameColumnName))
          executeEvaluation(dataFrame, nameColumnName, predictionColumnName)
        }
      }
    }
    "return report for target column of type Double" in {
      val report = executeEvaluation(testDataFrame, targetDoubleColumnName, predictionColumnName)
      report shouldBe expectedReport
    }
    "return report for for Boolean target column" in {
      val report = executeEvaluation(testDataFrame, targetBooleanColumnName, predictionColumnName)
      report shouldBe expectedReport
    }
    "return report for 2-categorical target column" in {
      val dataFrame: DataFrame =
        createDataFrame(correctRows, schema, Seq(target2CategoricalColumnName))
      val report = executeEvaluation(dataFrame, target2CategoricalColumnName, predictionColumnName)
      report shouldBe expectedReport
    }
  }

  private def expectedReport: Report = {
    val accuracyTable = Table(
      "accuracy",
      "Accuracy",
      Some(
        List(
          "Threshold",
          "Accuracy")),
      None,
      List(
        List(Some("0.99"), Some("1")),
        List(Some("0.01"), Some("0.5")))
    )

    val fMeasureByThresholdTable = Table(
      "fMeasureByThreshold",
      "F-Measure (F1 score) by threshold",
      Some(
        List(
          "Threshold",
          "F-Measure")),
      None,
      List(
        List(Some("0.99"), Some("1")),
        List(Some("0.01"), Some("0.666667")))
    )

    val rocTable = Table(
      "roc",
      "Receiver Operating Characteristic curve",
      Some(
        List(
          "False positive rate",
          "True positive rate")),
      None,
      List(
        List(Some("0"), Some("0")),
        List(Some("0"), Some("1")),
        List(Some("1"), Some("1")),
        List(Some("1"), Some("1")))
    )

    val summaryTable = Table(
      "summary",
      "Evaluate classification summary",
      Some(
        List(
          "DataFrame Size",
          "AUC",
          "Logarithmic Loss")),
      None,
      List(
        List(Some("24"), Some("1"), Some("0.01005")))
    )

    Report(ReportContent(
      "Evaluate Classification Report",
      List(summaryTable, accuracyTable, fMeasureByThresholdTable, rocTable)))
  }

  private def executeEvaluation(
      dataFrame: DataFrame,
      targetColumnName: String,
      predictionColumnName: String): Report = {
    val operation = EvaluateClassification(targetColumnName, predictionColumnName)
    val resultVector = operation.execute(executionContext)(Vector(dataFrame))
    resultVector.head.asInstanceOf[Report]
  }


}
