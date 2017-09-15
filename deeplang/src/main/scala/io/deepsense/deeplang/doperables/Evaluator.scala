/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.StructType

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters.{ColumnType, ParametersSchema, SingleColumnSelectorParameter}
import io.deepsense.deeplang.{DOperation1To1, ExecutionContext}

trait Evaluator extends DOperation1To1[DataFrame, Report] {

  val evaluatorParameters = ParametersSchema(
    Evaluator.targetColumnParamKey ->
      SingleColumnSelectorParameter("Target Column", required = true),
    Evaluator.predictionColumnParamKey ->
      SingleColumnSelectorParameter("Prediction Column", required = true)
  )

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): Report = {
    logger.info("Execution of " + this.getClass.getSimpleName + " starts")
    val predictionsAndLabels = getPredictionsAndLabels(dataFrame)
    logger.info("Preparing evaluation report")
    val evaluationReport = report(dataFrame, predictionsAndLabels)
    logger.info("Execution of " + this.getClass.getSimpleName + " ends")
    evaluationReport
  }

  protected def report(dataFrame: DataFrame, predictionsAndLabels: RDD[(Double, Double)]): Report

  protected def getPredictionsAndLabels(
      dataFrame: DataFrame): RDD[(Double, Double)] = {
    val predictionColumnName: String =
      columnName(dataFrame, Evaluator.predictionColumnParamKey)
    val targetColumnName: String = columnName(dataFrame, Evaluator.targetColumnParamKey)
    dataFrame.sparkDataFrame.select(predictionColumnName, targetColumnName).rdd.map { r =>
      (r.getDouble(0), r.getDouble(1))
    }
  }

  private def columnName(
      dataFrame: DataFrame,
      columnParamKey: String): String = {
    val colName = dataFrame.getColumnName(parameters.getSingleColumnSelection(columnParamKey).get)
    DataFrame.assertExpectedColumnType(dataFrame.sparkDataFrame.schema(colName), ColumnType.numeric)
    colName
  }
}

object Evaluator {
  val targetColumnParamKey = "targetColumn"
  val predictionColumnParamKey = "predictionColumn"
}
