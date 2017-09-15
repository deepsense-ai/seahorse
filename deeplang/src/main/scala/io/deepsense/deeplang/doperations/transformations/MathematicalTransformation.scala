/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations.transformations

import io.deepsense.deeplang.{DOperable, DMethod1To1, ExecutionContext}
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.{Report, Transformation}
import io.deepsense.deeplang.doperations.exceptions.MathematicalOperationExecutionException
import io.deepsense.reportlib.model.ReportContent

class MathematicalTransformation(formula: Option[String]) extends Transformation {

  def this() = this(None)

  override def toInferrable: DOperable = new MathematicalTransformation()

  override val transform = new DMethod1To1[Unit, DataFrame, DataFrame] {
    override def apply(context: ExecutionContext)(p: Unit)(dataFrame: DataFrame): DataFrame = {
      val transformedSparkDataFrame = try {
        dataFrame.sparkDataFrame.selectExpr("*", formula.get)
      } catch {
        case e: Exception => throw new MathematicalOperationExecutionException(formula.get, Some(e))
      }
      context.dataFrameBuilder.buildDataFrame(transformedSparkDataFrame)
    }
  }

  override def report: Report = Report(ReportContent("Report for MathematicalTransformation.\n" +
    s"Formula: $formula"))

  override def save(executionContext: ExecutionContext)(path: String): Unit = ???
}
