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

import scala.collection.immutable.ListMap
import scala.reflect.runtime.{universe => ru}

import io.deepsense.deeplang.DOperation._
import io.deepsense.deeplang.doperations.MissingValuesHandler.Strategy
import io.deepsense.deeplang.inference.{InferenceWarnings, InferContext}
import io.deepsense.deeplang.parameters.{MultipleColumnSelection, ChoiceParameter, ParametersSchema, ColumnSelectorParameter}
import io.deepsense.deeplang.{DKnowledge, ExecutionContext, DOperation1To1}
import io.deepsense.deeplang.doperables.dataframe.DataFrame

case class MissingValuesHandler() extends DOperation1To1[DataFrame, DataFrame] {

  override val name: String = "Missing Values Handler"
  override val id: Id = "e1120fbc-375b-4967-9c23-357ab768272f"

  val selectedColumnsParam = ColumnSelectorParameter(
    "Columns containing missing values to handle", required = true, portIndex = 0)

  val defaultStrategy = Strategy.REMOVE_ROW

  val strategyParam = ChoiceParameter(
    "Strategy of handling missing values",
    default = Some(defaultStrategy.toString),
    required = true,
    options = ListMap(
      MissingValuesHandler.Strategy.REMOVE_ROW.toString -> ParametersSchema())
  )

  override val parameters: ParametersSchema = ParametersSchema(
    "columns" -> selectedColumnsParam,
    "strategy" -> strategyParam
  )

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): DataFrame = {
    val strategy = Strategy.withName(strategyParam.value.get)
    val columns = dataFrame.getColumnNames(selectedColumnsParam.value.get)
    val retainedRows = strategy match {
      case Strategy.REMOVE_ROW => removeRowsWithEmptyValues(dataFrame, columns)
    }
    context.dataFrameBuilder.buildDataFrame(dataFrame.sparkDataFrame.schema, retainedRows)
  }

  override protected def _inferFullKnowledge(context: InferContext)
                                            (knowledge: DKnowledge[DataFrame])
    : ((DKnowledge[DataFrame]), InferenceWarnings) = {
    // TODO Check column selection correctness (this applies to other uoperations as well)
    ((knowledge), InferenceWarnings.empty)
  }

  private def removeRowsWithEmptyValues(dataFrame: DataFrame, columns: Seq[String]) = {
    dataFrame.sparkDataFrame.rdd.filter(
      row => !row.getValuesMap(columns).values.toList.contains(null))
  }

  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}

object MissingValuesHandler {
  class Strategy extends Enumeration {
    type Strategy = Value
    val REMOVE_ROW = Value("remove row")
  }

  object Strategy extends Strategy

  def apply(columns: MultipleColumnSelection, strategy: Strategy.Value): MissingValuesHandler = {
    val handler = MissingValuesHandler()
    handler.selectedColumnsParam.value = Some(columns)
    handler.strategyParam.value = Some(strategy.toString)
    handler
  }
}
