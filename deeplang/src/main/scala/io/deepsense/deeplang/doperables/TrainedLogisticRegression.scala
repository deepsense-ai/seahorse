/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.{DMethod1To1, ExecutionContext}

case class TrainedLogisticRegression() extends LogisticRegression with Scorable {
  override val score: DMethod1To1[Unit, DataFrame, DataFrame] = ???

  override def report: Report = ???

  override def save(executionContext: ExecutionContext)(path: String): Unit = ???
}
