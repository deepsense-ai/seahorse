/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

class TrainedRidgeRegressionSpec extends ScorableSpec[TrainedRidgeRegression]{
  def scorableName: String = "TrainedRidgeRegression"

  def scorable: Scorable = new TrainedRidgeRegression()
}
