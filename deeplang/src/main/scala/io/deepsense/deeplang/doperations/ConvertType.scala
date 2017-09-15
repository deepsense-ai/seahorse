/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.{ExecutionContext, DOperation1To1}
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters.{ColumnType, ChoiceParameter, ColumnSelectorParameter, ParametersSchema}

class ConvertType extends DOperation1To1[DataFrame, DataFrame] {

  override val name: String = "Convert Type"
  override val id: Id = "f8b3c5d0-febe-11e4-b939-0800200c9a66"
  override val parameters: ParametersSchema = ParametersSchema(
    ConvertType.SelectedColumns -> ColumnSelectorParameter(
      "Columns to be converted",
      required = true
    ),
    ConvertType.TargetType -> ChoiceParameter(
      "Target type of the columns",
      None,
      required = true,
      options = ColumnType.values.map(v => v.toString -> ParametersSchema()).toMap
    )
  )
  override protected def _execute(context: ExecutionContext)(t0: DataFrame): DataFrame = ???
}

object ConvertType {
  val SelectedColumns = "selectedColumns"
  val TargetType = "targetType"
}
