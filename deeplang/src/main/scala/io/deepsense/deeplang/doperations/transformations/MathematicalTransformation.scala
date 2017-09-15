/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations.transformations

import io.deepsense.deeplang.inference.{InferenceWarnings, InferContext}
import io.deepsense.deeplang.{DKnowledge, DOperable, DMethod1To1, ExecutionContext}
import io.deepsense.deeplang.doperables.dataframe.{DataFrameBuilder, DataFrame}
import io.deepsense.deeplang.doperables.{Report, Transformation}
import io.deepsense.deeplang.doperations.exceptions.MathematicalOperationExecutionException
import io.deepsense.reportlib.model.ReportContent

case class MathematicalTransformation(formula: Option[String]) extends Transformation {

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

    override def inferFull(
        context: InferContext)(
        p: Unit)(
        dataFrameKnowledge: DKnowledge[DataFrame])
        : (DKnowledge[DataFrame], InferenceWarnings) = {
      val dataFrame = dataFrameKnowledge.types.head

      // For now, we only say that a column is appended.
      val outputMetadata = dataFrame.inferredMetadata.get.copy(
        isExact = false, isColumnCountExact = false)

      val outputDataFrame = DataFrameBuilder.buildDataFrameForInference(outputMetadata)
      (DKnowledge(outputDataFrame), InferenceWarnings.empty)
    }
  }

  override def report: Report = Report(ReportContent("Report for MathematicalTransformation.\n" +
    s"Formula: $formula"))

  override def save(executionContext: ExecutionContext)(path: String): Unit = ???
}
