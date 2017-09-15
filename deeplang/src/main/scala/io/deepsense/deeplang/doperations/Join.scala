/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang.{DOperation2To1, ExecutionContext}

class Join extends DOperation2To1[DataFrame, DataFrame, DataFrame] {

  override val name: String = "Join"
  override val id: Id = "06374446-3138-4cf7-9682-f884990f3a60"
  val joinColumns = "joinColumns"

  override val parameters: ParametersSchema = ParametersSchema(
    joinColumns -> ColumnSelectorParameter(
      "Columns to be LEFT JOINed upon",
      required = true
    )
  )

  override protected def _execute(context: ExecutionContext)
    (ldf: DataFrame, rdf: DataFrame): DataFrame = ???
}
