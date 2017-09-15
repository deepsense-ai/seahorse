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

import scala.reflect.runtime.{universe => ru}

import org.apache.spark.rdd.RDD

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters.{ColumnType, ParametersSchema, SingleColumnSelection, SingleColumnSelectorParameter}
import io.deepsense.deeplang.{DOperation1To1, ExecutionContext}

trait Evaluator extends DOperation1To1[DataFrame, Report] with EvaluatorParams {

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): Report = {
    logger.debug("Execution of " + this.getClass.getSimpleName + " starts")
    val predictionsAndLabels = getPredictionsAndLabels(dataFrame)
    logger.debug("Preparing evaluation report")
    val evaluationReport = report(predictionsAndLabels)
    logger.debug("Execution of " + this.getClass.getSimpleName + " ends")
    evaluationReport
  }

  protected def report(predictionsAndLabels: RDD[(Double, Double)]): Report

  protected def getPredictionsAndLabels(dataFrame: DataFrame): RDD[(Double, Double)] = {
    val predictionColumnName = columnName(dataFrame, predictionColumnParameter.value.get)
    val targetColumnName: String = columnName(dataFrame, targetColumnParameter.value.get)

    dataFrame.sparkDataFrame
      .select(predictionColumnName, targetColumnName)
      .rdd
      .map { r => (r.getDouble(0), r.getDouble(1))
    }
  }

  private def columnName(
      dataFrame: DataFrame,
      columnSelection: SingleColumnSelection): String = {
    val name = dataFrame.getColumnName(columnSelection)
    DataFrame.assertExpectedColumnType(dataFrame.sparkDataFrame.schema(name), ColumnType.numeric)

    name
  }

  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTO_0: ru.TypeTag[Report] = ru.typeTag[Report]
}

trait EvaluatorParams {
  val targetColumnParameter = SingleColumnSelectorParameter(
    "Target Column",
    required = true,
    portIndex = 0)

  val predictionColumnParameter = SingleColumnSelectorParameter(
    "Prediction Column",
    required = true,
    portIndex = 0)

  val parameters = ParametersSchema(
    "Target Column" -> targetColumnParameter,
    "Prediction Column" -> predictionColumnParameter
  )
}
