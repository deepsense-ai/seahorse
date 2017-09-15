/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.{ExecutionContext, DOperation2To1}
import io.deepsense.deeplang.doperables.Scorable
import io.deepsense.deeplang.parameters.ParametersSchema

class ScoreRegressor extends DOperation2To1[Scorable, DataFrame, DataFrame]{
  override val id: DOperation.Id = "6cf6867c-e7fd-11e4-b02c-1681e6b88ec1"

  override val name = "Score regressor"

  override val parameters = ParametersSchema()

  override protected def _execute(
      context: ExecutionContext)(
      scorable: Scorable, dataframe: DataFrame): DataFrame = {
    scorable.score(context)(None)(dataframe)
  }
}
