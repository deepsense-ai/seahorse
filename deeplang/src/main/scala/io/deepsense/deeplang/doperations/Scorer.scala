/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.doperables.Scorable
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters.{SingleColumnCreatorParameter, ParametersSchema}
import io.deepsense.deeplang.{DOperation2To1, ExecutionContext}

trait Scorer[T <: Scorable] extends DOperation2To1[T, DataFrame, DataFrame] {

  val predictionColumnParam = SingleColumnCreatorParameter(
    "Column name for predictions",
    Some("prediction"),
    required = true)

  override val parameters = ParametersSchema(
    "prediction column" -> predictionColumnParam
  )

  override protected def _execute(
      context: ExecutionContext)(
      scorable: T, dataframe: DataFrame): DataFrame = {
    scorable.score(context)(predictionColumnParam.value.get)(dataframe)
  }
}
