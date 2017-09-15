/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}

import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.Report
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.exceptions.{ColumnDoesNotExistException, WrongColumnTypeException}
import io.deepsense.reportlib.model.{ReportContent, Table}

class EvaluateClassificationIntegSpec extends DeeplangIntegTestSupport {

  val nameColumnName = "name"
  val targetColumnName = "target"
  val predictionColumnName = "classificationPrediction"
  val schemaSeq = Seq(
    StructField(nameColumnName, StringType),
    StructField(targetColumnName, DoubleType),
    StructField(predictionColumnName, DoubleType))
  val schema = StructType(schemaSeq)
  val correctRows = Seq(
    Row("a", 1.0, 0.99),
    Row("b", 1.0, 0.99),
    Row("c", 1.0, 0.99),
    Row("d", 0.0, 0.01),
    Row("e", 1.0, 0.99),
    Row("f", 1.0, 0.99),
    Row("g", 0.0, 0.01),
    Row("h", 0.0, 0.01),
    Row("i", 0.0, 0.01),
    Row("j", 1.0, 0.99),
    Row("k", 1.0, 0.99),
    Row("l", 1.0, 0.99),
    Row("m", 0.0, 0.01),
    Row("n", 0.0, 0.01),
    Row("o", 0.0, 0.01),
    Row("p", 0.0, 0.01),
    Row("r", 0.0, 0.01),
    Row("s", 0.0, 0.01),
    Row("t", 0.0, 0.01),
    Row("u", 0.0, 0.01),
    Row("w", 1.0, 0.99),
    Row("x", 1.0, 0.99),
    Row("y", 1.0, 0.99),
    Row("z", 1.0, 0.99))

  "EvaluateClassification" should {
    def testDataFrame: DataFrame = createDataFrame(correctRows, schema)
    "throw exception" when {
      "selected target column is not Double" in {
        a[WrongColumnTypeException] should be thrownBy {
          executeEvaluation(testDataFrame, nameColumnName, predictionColumnName)
        }
      }
      "classification prediction column is not Double" in {
        a[WrongColumnTypeException] should be thrownBy {
          executeEvaluation(testDataFrame, targetColumnName, nameColumnName)
        }
      }
      "target column does not exist" in {
        a[ColumnDoesNotExistException] should be thrownBy {
          executeEvaluation(testDataFrame, "whatever", predictionColumnName)
        }
      }
      "classification prediction column does not exist" in {
        a[ColumnDoesNotExistException] should be thrownBy {
          executeEvaluation(testDataFrame, targetColumnName, "blah")
        }
      }
    }
    "return report for all correct classification predictions" in {
      val report = executeEvaluation(testDataFrame, targetColumnName, predictionColumnName)
      assertEvaluateClassificationReport(report, expectedReport)
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
          "DataFrame size",
          "AUC",
          "Logarithmic Loss")),
      None,
      List(
        List(Some("24"), Some("1"), Some("0.01005")))
    )

    Report(ReportContent(
      EvaluateClassification.ReportName,
      Map(
        summaryTable.name -> summaryTable,
        accuracyTable.name -> accuracyTable,
        fMeasureByThresholdTable.name -> fMeasureByThresholdTable,
        rocTable.name -> rocTable)
    ))
  }

  private def assertEvaluateClassificationReport(
      report: Report,
      expectedReport: Report): Registration = {
    report shouldBe expectedReport
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
