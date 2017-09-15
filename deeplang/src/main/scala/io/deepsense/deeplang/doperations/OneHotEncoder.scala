/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.DOperation._
import io.deepsense.deeplang.{ExecutionContext, DOperation1To1}
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters.{BooleanParameter, ColumnSelectorParameter, ParametersSchema}

case class OneHotEncoder() extends DOperation1To1[DataFrame, DataFrame] {
  override val name: String = "One Hot Encoder"
  override val id: Id = "b1b6eefe-f7b7-11e4-a322-1697f925ec7b"
  val selectedColumnsKey = "columns"
  val withRedundantKey = "with redundant"

  override val parameters: ParametersSchema = ParametersSchema(
    selectedColumnsKey -> ColumnSelectorParameter("Columns to encode", required = true),
    withRedundantKey -> BooleanParameter(
      "Preserve redundant column", default = Some(false), required = true
    )
  )

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): DataFrame = ???
}
