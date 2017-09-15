/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.classification.LogisticRegressionModel

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.{DMethod1To1, ExecutionContext}

case class TrainedLogisticRegression(
    model: Option[LogisticRegressionModel],
    featureColumns: Option[Seq[String]],
    targetColumn: Option[String]) extends LogisticRegression with Scorable {

  def this() = this(None, None, None)

  override val score: DMethod1To1[Unit, DataFrame, DataFrame] =
    new DMethod1To1[Unit, DataFrame, DataFrame] {
    override def apply(context: ExecutionContext)(parameters: Unit)(t0: DataFrame): DataFrame = ???
  }

  override def report: Report = ???

  override def save(executionContext: ExecutionContext)(path: String): Unit = {
    // TODO: Implement
  }
}
