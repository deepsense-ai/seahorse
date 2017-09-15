/**
 * Copyright (c) 2015, CodiLime, Inc.
 */
package io.deepsense.deeplang.doperations

import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.rdd.RDD

import io.deepsense.commons.utils.DoubleUtils
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.{Evaluator, Report}
import io.deepsense.deeplang.parameters.{NameSingleColumnSelection, ParametersSchema}
import io.deepsense.reportlib.model.{ReportContent, Table}

case class EvaluateClassification() extends Evaluator {

  override val name: String = "Evaluate Classification"

  override val id: Id = "1163bb76-ba65-4471-9632-dfb761d20dfb"

  override val parameters = evaluatorParameters

  override protected def report(
      dataFrame: DataFrame,
      predictionsAndLabels: RDD[(Double, Double)]): Report = {
    logger.info("Computing DataFrame size")
    val dataFrameSize = dataFrame.sparkDataFrame.count()
    logger.info("Preparing BinaryClassificationMetrics object")
    val metrics =
      new BinaryClassificationMetrics(predictionsAndLabels, EvaluateClassification.MetricsNumBins)

    logger.info("Computing LogarithmicLoss metric")
    val logLossSum = predictionsAndLabels.map {
      case (prediction, label) =>
        label * math.log(prediction) + (1.0 - label) * math.log(1.0 - prediction)
    }.sum()
    val logLoss = logLossSum * -1.0 / dataFrameSize

    logger.info("Computing accuracyByThreshold metric")
    // TODO: This implementation of accuracy computing is inefficient
    val predictionsAndBooleanLabels =
      predictionsAndLabels.map { case (prediction, label) => (prediction, label < 0.5) }
    predictionsAndBooleanLabels.cache()
    val accuracyByThreshold = metrics.thresholds().collect().map {
      threshold =>
        (threshold, accuracyForThreshold(threshold, predictionsAndBooleanLabels, dataFrameSize))
    }
    predictionsAndBooleanLabels.unpersist()
    val accuracyTable = Table(
      "accuracy",
      "Accuracy",
      Some(
        List(
          "Threshold",
          "Accuracy")),
      None,
      reportTableValues(accuracyByThreshold)
    )

    logger.info("Computing fMeasureByThreshold metric")
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

    logger.info("Computing ROC curve")
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

    logger.info("Preparing summary table")
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
        List(
          Some(dataFrameSize.toString),
          Some(DoubleUtils.double2String(metrics.areaUnderROC())),
          Some(DoubleUtils.double2String(logLoss))
        ))
    )

    logger.info("Assembling evaluation report")
    val report = Report(ReportContent(
      EvaluateClassification.ReportName,
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

  private def accuracyForThreshold(
      threshold: Double,
      predictionsAndBooleanLabels: RDD[(Double, Boolean)],
      dataFrameSize: Long): Double = {
    val eps = EvaluateClassification.Epsilon
    predictionsAndBooleanLabels.filter { case (prediction, label) =>
      (prediction < threshold) == label
    }.count() * 1.0 / dataFrameSize
  }

  private def reportTableValues(valuesRdd: Array[(Double, Double)]): List[List[Option[String]]] = {
    valuesRdd.map {
      case (a: Double, b: Double) =>
        List(Some(DoubleUtils.double2String(a)), Some(DoubleUtils.double2String(b)))
    }.toList
  }
}

object EvaluateClassification {

  private val Epsilon = 0.01

  private val MetricsNumBins = 10

  val ReportName = "evaluateClassification"

  def apply(
      targetColumnName: String,
      predictionColumnName: String): EvaluateClassification = {
    val operation = new EvaluateClassification
    val targetColumnParam =
      operation.parameters.getSingleColumnSelectorParameter(Evaluator.targetColumnParamKey)
    targetColumnParam.value = Some(NameSingleColumnSelection(targetColumnName))
    val predictionColumnParam =
      operation.parameters.getSingleColumnSelectorParameter(Evaluator.predictionColumnParamKey)
    predictionColumnParam.value = Some(NameSingleColumnSelection(predictionColumnName))
    operation
  }
}
