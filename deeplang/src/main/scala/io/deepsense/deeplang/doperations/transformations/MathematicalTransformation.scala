/**
 * Copyright (c) 2015, CodiLime Inc.
 */
package io.deepsense.deeplang.doperations.transformations

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.{Report, Transformation}
import io.deepsense.deeplang.doperables.dataframe.DataFrame

class MathematicalTransformation(formula: String) extends Transformation {
  override def transform(dataFrame: DataFrame): DataFrame = {
    DataFrame(Some(dataFrame.sparkDataFrame.selectExpr("*", formula)))
  }

  override def report: Report = ???

  override def save(executionContext: ExecutionContext)(path: String): Unit = ???
}
