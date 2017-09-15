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

import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.rdd.RDD

import io.deepsense.commons.utils.{DoubleUtils, Logging}
import io.deepsense.reportlib.model.{ReportContent, Table}

object RegressionReporter extends Reporter with Logging {
  import Reporter._

  override def report(predictionsAndLabels: RDD[(Double, Double)]): Report = {
    val rows =
      List(
        List(Some(predictionsAndLabels.count().toString))
          ++ getMetrics(predictionsAndLabels)
          .map(DoubleUtils.double2String)
          .map(Some.apply)
      )

    val table = Table(ReportName, ReportDescription, Some(ReportColumnNames), None, rows)
    Report(ReportContent(ReportName, List(table)))
  }

  override protected def createPartialReport(
      splitIndex: Int, foldData: CrossValidationFold): PartialRegressionReport =
    PartialRegressionReport(
      splitIndex,
      foldData.trainingDataFrameSize,
      foldData.testDataFrameSize,
      getMetrics(foldData.predictionsAndLabels))

  override protected def mergePartialReports(partialReports: Seq[PartialReport]): Report = {
    val rows = partialReports.map( partialReport => {
      val partial = partialReport.asInstanceOf[PartialRegressionReport]

      val splitIndexColumn = List(partial.splitIndex + 1).map(_.toString)

      val foldDescriptionColumns =
        List(partial.trainingDataFrameSize, partial.testDataFrameSize).map(_.toString)

      val metricsColumns = partial.results.map(DoubleUtils.double2String)

      (splitIndexColumn ++ foldDescriptionColumns ++ metricsColumns).map(Some.apply)
    }).toList

    val averageMetrics = partialReports
      .foldLeft(List(0.0, 0.0, 0.0, 0.0, 0.0)) {
      (acc, c) =>
        (acc zip c.asInstanceOf[PartialRegressionReport].results).map {
          case (a, b) => a + b / partialReports.size.toDouble
        }}
      .map(DoubleUtils.double2String)

    val averageRow = (List(AverageRowName, "", "") ++ averageMetrics).map(Some.apply)
    val rowsWithAverage = rows ++ List(averageRow)

    val rowNames = partialReports.map {
      case PartialRegressionReport(idx, _, _, _) => idx + 1
    }.toList.map(_.toString) ++ List(AverageRowName)

    val table = Table(
      CvReportTableName,
      CvReportDescription,
      Some(CvReportColumnNames),
      Some(rowNames),
      rowsWithAverage)

    Report(ReportContent(CvReportName, List(table)))
  }

  def getMetrics(predictionsAndLabels: RDD[(Double, Double)]): List[Double] = {
    val metrics = new RegressionMetrics(predictionsAndLabels)
    List(
      metrics.explainedVariance,
      metrics.meanAbsoluteError,
      metrics.meanSquaredError,
      metrics.r2,
      metrics.rootMeanSquaredError)
  }

  case class PartialRegressionReport(
      splitIndex: Int,
      trainingDataFrameSize: Long,
      testDataFrameSize: Long,
      results: List[Double])
    extends PartialReport(splitIndex, trainingDataFrameSize, testDataFrameSize)

  val ReportName = "Evaluate Regression Report"
  val ReportDescription = "Evaluate regression metrics"

  val CvReportName = "Cross-validate Regression Report"
  val CvReportDescription = "Cross-validate regression metrics"

  val MetricsColumnNames = List(
    "Explained Variance",
    "Mean Absolute Error",
    "Mean Squared Error",
    "r2",
    "Root Mean Squared Error"
  )

  val ReportColumnNames = List("DataFrame Size") ++ MetricsColumnNames
  val CvReportColumnNames =
    List("Fold Number", "Training Set Size", "Test Set Size") ++ MetricsColumnNames

  val CvReportTableName = "Cross-validate Regression Report"

  val AverageRowName = "average"
}
