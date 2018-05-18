/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperables

import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.report.{CommonTablesGenerators, Report}
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import ai.deepsense.deeplang.params.Params
import ai.deepsense.deeplang.{DKnowledge, DMethod1To1, DOperable, ExecutionContext}
import ai.deepsense.reportlib.model.ReportType

/**
 * Evaluates a DataFrame.
 */
abstract class Evaluator extends DOperable with Params {

  /**
   * Evaluates a DataFrame.
   */
  private[deeplang] def _evaluate(context: ExecutionContext, dataFrame: DataFrame): MetricValue

  private[deeplang] def _infer(k: DKnowledge[DataFrame]): MetricValue

  def evaluate: DMethod1To1[Unit, DataFrame, MetricValue] = {
    new DMethod1To1[Unit, DataFrame, MetricValue] {
      override def apply(ctx: ExecutionContext)(p: Unit)(dataFrame: DataFrame): MetricValue = {
        _evaluate(ctx, dataFrame)
      }

      override def infer(ctx: InferContext)(p: Unit)(k: DKnowledge[DataFrame])
      : (DKnowledge[MetricValue], InferenceWarnings) = {
        (DKnowledge(_infer(k)), InferenceWarnings.empty)
      }
    }
  }

  override def report(extended: Boolean = true): Report =
    super.report(extended)
      .withReportName(s"${this.getClass.getSimpleName} Report")
      .withReportType(ReportType.Evaluator)
      .withAdditionalTable(CommonTablesGenerators.params(extractParamMap()))

  def isLargerBetter: Boolean
}
