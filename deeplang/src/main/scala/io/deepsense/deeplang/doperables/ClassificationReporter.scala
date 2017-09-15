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

import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.rdd.RDD

import io.deepsense.commons.utils.{DoubleUtils, Logging}
import io.deepsense.reportlib.model.{ReportContent, Table}

object ClassificationReporter extends Reporter with Logging {
  import Reporter._

  override def report(predictionsAndLabels: RDD[(Double, Double)]): Report = {
    logger.debug("Computing DataFrame size")
    val dataFrameSize = predictionsAndLabels.count()

    logger.debug("Preparing BinaryClassificationMetrics object")
    val metrics =
      new BinaryClassificationMetrics(predictionsAndLabels, MetricsNumBins)

    val accuracyTable = Table(
      "accuracy",
      "Accuracy",
      Some(
        List(
          "Threshold",
          "Accuracy")),
      None,
      reportTableValues(accuracyByThreshold(dataFrameSize, metrics, predictionsAndLabels))
    )

    logger.debug("Computing fMeasureByThreshold metric")
    val fMeasureByThresholdTable = Table(
      "fMeasureByThreshold",
      "F-Measure (F1 score) by threshold",
      Some(
        List(
          "Threshold",
          "F-Measure")),
      None,
      reportTableValues(metrics.fMeasureByThreshold().collect())
    )

    logger.debug("Computing Receiver Operating Characteristic curve")
    val rocTable = Table(
      "roc",
      "Receiver Operating Characteristic curve",
      Some(
        List(
          "False positive rate",
          "True positive rate")),
      None,
      reportTableValues(metrics.roc().collect())
    )

    logger.debug("Preparing summary table")
    val summaryTable = Table(
      "summary",
      SummaryDescription,
      Some(SummaryColumnNames),
      None,
      List(
        List(
          Some(dataFrameSize.toString),
          Some(DoubleUtils.double2String(metrics.areaUnderROC())),
          Some(DoubleUtils.double2String(logLoss(dataFrameSize, predictionsAndLabels)))
        ))
    )

    logger.debug("Assembling evaluation report")
    val report = Report(ReportContent(
      ReportName,
      Map(
        summaryTable.name -> summaryTable,
        accuracyTable.name -> accuracyTable,
        fMeasureByThresholdTable.name -> fMeasureByThresholdTable,
        rocTable.name -> rocTable)
    ))

    import spray.json._

    import io.deepsense.reportlib.model.ReportJsonProtocol._
    logger.debug("EvaluateClassification report = " + report.content.toJson.prettyPrint)
    report
  }

  override protected def createPartialReport(
      splitIndex: Int, foldData: CrossValidationFold): PartialClassificationReport = {

    logger.debug("Computing DataFrame size for partial report")
    val dataFrameSize = foldData.predictionsAndLabels.count()

    logger.debug("Preparing BinaryClassificationMetrics object for partial report")
    val metrics =
      new BinaryClassificationMetrics(foldData.predictionsAndLabels, MetricsNumBins)

    PartialClassificationReport(
      splitIndex,
      foldData.trainingDataFrameSize,
      foldData.testDataFrameSize,
      logLoss(dataFrameSize, foldData.predictionsAndLabels),
      metrics.areaUnderROC(),
      accuracyForThreshold(dataFrameSize, 0.5, foldData.predictionsAndLabels)
    )
  }

  override protected def mergePartialReports(partialReports: Seq[PartialReport]): Report = {
    val rows = partialReports.map( partialReport => {
      val partial = partialReport.asInstanceOf[PartialClassificationReport]

      val splitIndexColumn = List(partial.splitIndex + 1).map(_.toString)

      val foldDescriptionColumns =
        List(partial.trainingDataFrameSize, partial.testDataFrameSize).map(_.toString)

      val metricsColumns = List(partial.logLoss, partial.areaUnderROC, partial.accuracy)
        .map(DoubleUtils.double2String)

      (splitIndexColumn ++ foldDescriptionColumns ++ metricsColumns).map(Some.apply)
    }).toList : List[List[Option[String]]]

    val rowNames = partialReports.map {
      case PartialClassificationReport(idx, _, _, _, _, _) => idx + 1
    }.toList.map(_.toString)

    val table = Table(
      CvSummaryTableName,
      CvSummaryDescription,
      Some(CvSummaryColumnNames),
      Some(rowNames),
      rows)

    Report(ReportContent(CvReportName, Map(table.name -> table)))
  }

  case class PartialClassificationReport(
      splitIndex: Int,
      trainingDataFrameSize: Long,
      testDataFrameSize: Long,
      logLoss: Double,
      areaUnderROC: Double,
      accuracy: Double)
    extends PartialReport(splitIndex, trainingDataFrameSize, testDataFrameSize)

  private def accuracyByThreshold(
      dataFrameSize: Long,
      metrics: BinaryClassificationMetrics,
      predictionsAndLabels: RDD[(Double, Double)]): Array[(Double, Double)] = {

    logger.debug("Computing accuracyByThreshold metric")

    predictionsAndLabels.cache()

    val accuracyByThreshold = metrics.thresholds().collect().map { threshold =>
        (threshold, accuracyForThreshold(dataFrameSize, threshold, predictionsAndLabels))
    }

    predictionsAndLabels.unpersist()

    accuracyByThreshold
  }

  private def accuracyForThreshold(
      dataFrameSize: Long,
      threshold: Double,
      predictionsAndLabels: RDD[(Double, Double)]): Double = {

    predictionsAndLabels.filter {
      case (prediction, label) => (prediction < threshold) == (label < 0.5)
    }.count().toDouble / dataFrameSize.toDouble
  }

  private def logLoss(
      dataFrameSize: Long,
      predictionsAndLabels: RDD[(Double, Double)]): Double = {

    logger.debug("Computing LogarithmicLoss metric")
    predictionsAndLabels.map {
      case (prediction, label) =>
        label * math.log(prediction) + (1.0 - label) * math.log(1.0 - prediction)
    }.sum() * -1.0 / dataFrameSize
  }

  private def reportTableValues(valuesRdd: Array[(Double, Double)]): List[List[Option[String]]] = {
    valuesRdd.map {
      case (a: Double, b: Double) =>
        List(Some(DoubleUtils.double2String(a)), Some(DoubleUtils.double2String(b)))
    }.toList
  }

  private val MetricsNumBins = 10

  val ReportName = "Evaluate Classification Report"
  val SummaryDescription = "Evaluate classification summary"

  val CvReportName = "Cross-validate Classification Report"
  val CvSummaryDescription = "Cross-validate classification summary"

  val SummaryMetricsColumnNames = List(
    "AUC",
    "Logarithmic Loss"
  )

  val CvSummaryMetricsColumnNames = SummaryMetricsColumnNames ++ List("Accuracy")

  val SummaryColumnNames = List("DataFrame Size") ++ SummaryMetricsColumnNames
  val CvSummaryColumnNames =
    List("Fold Number", "Training Set Size", "Test Set Size") ++ CvSummaryMetricsColumnNames

  val CvSummaryTableName = "Cross-validate Classification Report"
}
