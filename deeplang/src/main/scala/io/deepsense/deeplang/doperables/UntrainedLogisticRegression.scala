/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import io.deepsense.deeplang.doperables.Trainable.Parameters
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.{DMethod1To1, ExecutionContext}

case class UntrainedLogisticRegression() extends LogisticRegression with Trainable {
  override val train: DMethod1To1[Parameters, DataFrame, Scorable] = ???

  override def report: Report = ???

  override def save(executionContext: ExecutionContext)(path: String): Unit = ???
}
