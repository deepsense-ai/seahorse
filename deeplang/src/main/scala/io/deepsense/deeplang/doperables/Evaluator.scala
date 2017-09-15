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

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.StructType

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters.{ColumnType, ParametersSchema, SingleColumnSelectorParameter}
import io.deepsense.deeplang.{DOperation1To1, ExecutionContext}

trait Evaluator extends DOperation1To1[DataFrame, Report] {

  val evaluatorParameters = ParametersSchema(
    Evaluator.targetColumnParamKey ->
      SingleColumnSelectorParameter("Target Column", required = true, portIndex = 0),
    Evaluator.predictionColumnParamKey ->
      SingleColumnSelectorParameter("Prediction Column", required = true, portIndex = 0)
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
  val targetColumnParamKey = "target column"
  val predictionColumnParamKey = "prediction column"
}
