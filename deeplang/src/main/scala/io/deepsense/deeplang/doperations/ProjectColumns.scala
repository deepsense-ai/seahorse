/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang.{DOperation1To1, ExecutionContext}

case class ProjectColumns() extends DOperation1To1[DataFrame, DataFrame] {

  override val name: String = "Project Columns"
  override val id: Id = "96b28b7f-d54c-40d7-9076-839411793d20"
  val selectedColumns = "selectedColumns"

  override val parameters: ParametersSchema = ParametersSchema(
    selectedColumns -> ColumnSelectorParameter(
      "Columns to be included in the output DataFrame",
      required = true,
      portIndex = 0
    )
  )

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): DataFrame = {
    val columns = dataFrame.getColumnNames(parameters.getColumnSelection(selectedColumns).get)
    if (columns.nonEmpty) {
      val projected = dataFrame.sparkDataFrame.select(columns.head, columns.tail: _*)
      context.dataFrameBuilder.buildDataFrame(projected)
    } else {
      DataFrame.empty(context)
    }
  }
}
