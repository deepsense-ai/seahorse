/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.doperables.Scorable
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters.ParametersSchema
import io.deepsense.deeplang.{DOperation2To1, ExecutionContext}

trait Scorer[T <: Scorable] extends DOperation2To1[T, DataFrame, DataFrame] {
  override val parameters = ParametersSchema()

  override protected def _execute(
    context: ExecutionContext)(
    scorable: T, dataframe: DataFrame): DataFrame = {

    scorable.score(context)(None)(dataframe)
  }
}
