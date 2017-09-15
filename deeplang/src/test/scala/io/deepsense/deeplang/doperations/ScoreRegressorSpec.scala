/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.doperables.{Regressor, Scorable}

class ScoreRegressorSpec extends ScorerSpec[Scorable with Regressor] {

  override def scorer = ScoreRegressor()

  override def scorerName = "ScoreRegressor"
}
