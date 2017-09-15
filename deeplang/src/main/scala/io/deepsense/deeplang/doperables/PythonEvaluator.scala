/**
 * Copyright 2016, deepsense.io
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

package io.deepsense.deeplang.doperables

import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.dataframe._
import io.deepsense.deeplang.doperables.spark.wrappers.params.common.HasIsLargerBetterParam
import io.deepsense.deeplang.doperations.exceptions.CustomOperationExecutionException
import io.deepsense.deeplang.params.{CodeSnippetLanguage, CodeSnippetParam, Param, StringParam}

case class PythonEvaluator()
    extends Evaluator
    with HasIsLargerBetterParam {

  val InputPortNumber: Int = 0
  val OutputPortNumber: Int = 0

  val metricName = StringParam(
    name = "metric name",
    description = "Name of the metric.")
  setDefault(metricName -> "custom metric")
  def getMetricName: String = $(metricName)

  val codeParameter = CodeSnippetParam(
    name = "python evaluator code",
    description = "Python evaluator source code.",
    language = CodeSnippetLanguage(CodeSnippetLanguage.python))
  setDefault(codeParameter ->
    """from math import sqrt
      |from operator import add
      |
      |def evaluate(dataframe):
      |    # Example Root-Mean-Square Error implementation
      |    n = dataframe.count()
      |    row_to_sq_error = lambda row: (row['label'] - row['prediction'])**2
      |    sum_sq_error = dataframe.map(row_to_sq_error).reduce(add)
      |    rmse = sqrt(sum_sq_error / n)
      |    return rmse""".stripMargin)

  override def params: Array[Param[_]] =
    declareParams(metricName, codeParameter, isLargerBetterParam)

  override def isLargerBetter: Boolean = $(isLargerBetterParam)

  override def _evaluate(ctx: ExecutionContext, df: DataFrame): MetricValue = {
    val userCode = $(codeParameter)

    val composedCode = getComposedCode(userCode)

    if (!ctx.pythonCodeExecutor.isValid(composedCode)) {
      throw CustomOperationExecutionException("Code validation failed")
    }

    ctx.dataFrameStorage.withInputDataFrame(InputPortNumber, df.sparkDataFrame) {
      ctx.pythonCodeExecutor.run(composedCode) match {
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

  // Creating a dataframe is a workaround. Currently we can pass to jvm DataFrames only.
  // TODO DS-3695 Fix a metric value - dataframe workaround.
  private def getComposedCode(userCode: String): String = {
      s"""
      |
      |$userCode
      |
      |def transform(dataframe):
      |    result = evaluate(dataframe)
      |    try:
      |        float_result = float(result)
      |    except ValueError:
      |        raise Exception("Invalid result of `evaluate` function. " +
      |                        "Value " + str(result) + " of type " +
      |                        str(type(result)) + " cannot be converted to float.")
      |    except TypeError:
      |        raise Exception("Invalid result type of `evaluate` function. " +
      |                        "Type " + str(type(result)) +
      |                        " cannot be cast to float.")
      |    result_df = sqlContext.createDataFrame([[float_result]])
      |    return result_df
      """.stripMargin
      }

  override def _infer(k: DKnowledge[DataFrame]): MetricValue =
    MetricValue.forInference(getMetricName)
}
