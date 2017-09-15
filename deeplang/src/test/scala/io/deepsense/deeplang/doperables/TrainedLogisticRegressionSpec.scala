/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

class TrainedLogisticRegressionSpec extends ScorableSpec[TrainedLogisticRegression]{
  def scorableName: String = "TrainedLogisticRegression"

  def scorable: Scorable = new TrainedLogisticRegression()
}
