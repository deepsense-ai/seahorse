/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.deeplang.doperations

import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}

import io.deepsense.commons.utils.DoubleUtils
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.Report
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.exceptions.WrongColumnTypeException
import io.deepsense.deeplang.parameters.{ColumnType, ParametersSchema, SingleColumnSelectorParameter}
import io.deepsense.deeplang.{DOperation1To1, ExecutionContext}
import io.deepsense.reportlib.model.{ReportContent, Table}

class EvaluateRegression() extends DOperation1To1[DataFrame, Report] {
  override val name: String = "Evaluate Regression"
  override val id: Id = "f2a43e21-331e-42d3-8c02-7db1da20bc00"
  override val parameters = ParametersSchema(
    EvaluateRegression.targetColumnParamKey ->
      SingleColumnSelectorParameter("Target Column", required = true),
    EvaluateRegression.predictionColumnParamKey ->
      SingleColumnSelectorParameter("Prediction Column", required = true)
  )

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): Report = {
    val schema: StructType = dataFrame.sparkDataFrame.schema
    val targetColumnName: String = columnName(dataFrame, EvaluateRegression.targetColumnParamKey)
    val predictionColumnName: String =
      columnName(dataFrame, EvaluateRegression.predictionColumnParamKey)
    val labels = dataFrame.sparkDataFrame.select(targetColumnName).rdd.map(_.getDouble(0))
    val predictions = dataFrame.sparkDataFrame.select(predictionColumnName).rdd.map(_.getDouble(0))
    val metrics = new RegressionMetrics(predictions zip labels)
    report(dataFrame.sparkDataFrame.count(), metrics)
  }

  private def columnName(dataFrame: DataFrame, columnParamKey: String): String = {
    val colName = dataFrame.getColumnName(parameters.getSingleColumnSelection(columnParamKey).get)
    DataFrame.assertExpectedColumnType(dataFrame.sparkDataFrame.schema(colName), ColumnType.numeric)
    colName
  }

  private def report(dataFrameSize: Long, metrics: RegressionMetrics): Report = {
    val evaluateRegressionName: String = "EvaluateRegression"
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
  val targetColumnParamKey = "targetColumn"
  val predictionColumnParamKey = "predictionColumn"
}
