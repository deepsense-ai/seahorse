/**
 * Copyright (c) 2015, CodiLime, Inc.
 */
package io.deepsense.deeplang.doperations

import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.rdd.RDD

import io.deepsense.commons.utils.DoubleUtils
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.{Evaluator, Report}
import io.deepsense.deeplang.parameters.NameSingleColumnSelection
import io.deepsense.reportlib.model.{ReportContent, Table}

case class EvaluateRegression() extends Evaluator {

  override val name: String = "Evaluate Regression"

  override val id: Id = "f2a43e21-331e-42d3-8c02-7db1da20bc00"

  override val parameters = evaluatorParameters

  override protected def report(
      dataFrame: DataFrame,
      predictionsAndLabels: RDD[(Double, Double)]): Report = {
    val dataFrameSize = dataFrame.sparkDataFrame.count()
    val metrics = new RegressionMetrics(predictionsAndLabels)
    val evaluateRegressionName: String = "Evaluate Regression Report"
    val table = Table(
      evaluateRegressionName,
      "Evaluate regression metrics",
      Some(
        List(
          "DataFrame Size",
          "Explained Variance",
          "Mean Absolute Error",
          "Mean Squared Error",
          "r2",
          "Root Mean Squared Error")),
      None,
      List(
        List(
          Some(dataFrameSize.toString),
          Some(DoubleUtils.double2String(metrics.explainedVariance)),
          Some(DoubleUtils.double2String(metrics.meanAbsoluteError)),
          Some(DoubleUtils.double2String(metrics.meanSquaredError)),
          Some(DoubleUtils.double2String(metrics.r2)),
          Some(DoubleUtils.double2String(metrics.rootMeanSquaredError))
        ))
    )
    Report(ReportContent(evaluateRegressionName, Map(table.name -> table)))
  }
}

object EvaluateRegression {
  def apply(
      targetColumnName: String,
      predictionColumnName: String): EvaluateRegression = {
    val operation = new EvaluateRegression
    val targetColumnParam =
      operation.parameters.getSingleColumnSelectorParameter(Evaluator.targetColumnParamKey)
    targetColumnParam.value = Some(NameSingleColumnSelection(targetColumnName))
    val predictionColumnParam =
      operation.parameters.getSingleColumnSelectorParameter(Evaluator.predictionColumnParamKey)
    predictionColumnParam.value = Some(NameSingleColumnSelection(predictionColumnName))
    operation
  }
}
