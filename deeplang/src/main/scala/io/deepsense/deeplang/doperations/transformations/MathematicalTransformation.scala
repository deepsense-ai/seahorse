/**
 * Copyright 2015, CodiLime Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.deeplang.doperations.transformations

import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameBuilder}
import io.deepsense.deeplang.doperables.{Report, Transformation}
import io.deepsense.deeplang.doperations.exceptions.MathematicalOperationExecutionException
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.{DKnowledge, DMethod1To1, DOperable, ExecutionContext}
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
