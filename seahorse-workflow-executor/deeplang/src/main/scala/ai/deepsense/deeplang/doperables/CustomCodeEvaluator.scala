/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

import ai.deepsense.deeplang.OperationExecutionDispatcher.Result
import ai.deepsense.deeplang.{DKnowledge, ExecutionContext}
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.HasIsLargerBetterParam
import ai.deepsense.deeplang.doperations.exceptions.CustomOperationExecutionException
import ai.deepsense.deeplang.params.{CodeSnippetParam, StringParam, Param}

abstract class CustomCodeEvaluator()
  extends Evaluator
    with HasIsLargerBetterParam {

  val InputPortNumber: Int = 0
  val OutputPortNumber: Int = 0

  val metricName = StringParam(
    name = "metric name",
    description = None)
  setDefault(metricName -> "custom metric")

  def getMetricName: String = $(metricName)

  val codeParameter: CodeSnippetParam

  override def params: Array[Param[_]] =
    Array(metricName, codeParameter, isLargerBetterParam)

  override def isLargerBetter: Boolean = $(isLargerBetterParam)

  def isValid(context: ExecutionContext, code: String): Boolean

  def runCode(context: ExecutionContext, code: String): Result

  def getComposedCode(userCode: String): String

  override def _evaluate(ctx: ExecutionContext, df: DataFrame): MetricValue = {
    val userCode = $(codeParameter)

    val composedCode = getComposedCode(userCode)

    if (!isValid(ctx, composedCode)) {
      throw CustomOperationExecutionException("Code validation failed")
    }

    ctx.dataFrameStorage.withInputDataFrame(InputPortNumber, df.sparkDataFrame) {
      runCode(ctx, composedCode) match {
        case Left(error) =>
          throw CustomOperationExecutionException(s"Execution exception:\n\n$error")

        case Right(_) =>
          val sparkDataFrame =
            ctx.dataFrameStorage.getOutputDataFrame(OutputPortNumber).getOrElse {
              throw CustomOperationExecutionException(
                "Function `evaluate` finished successfully, but did not produce a metric.")
            }

          val metricValue = sparkDataFrame.collect.head.getAs[Double](0)
          MetricValue(getMetricName, metricValue)
      }
    }
  }

  override def _infer(k: DKnowledge[DataFrame]): MetricValue =
    MetricValue.forInference(getMetricName)
}
