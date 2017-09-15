/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.doperables.{Regressor, Scorable}

case class ScoreRegressor() extends Scorer[Regressor with Scorable] {
  override val id: DOperation.Id = "6cf6867c-e7fd-11e4-b02c-1681e6b88ec1"
  override val name = "Score regressor"
}
