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

import ai.deepsense.deeplang.OperationExecutionDispatcher._
import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.params.{CodeSnippetLanguage, CodeSnippetParam}

class REvaluator extends CustomCodeEvaluator {

  override val codeParameter = CodeSnippetParam(
    name = "R evaluator code",
    description = None,
    language = CodeSnippetLanguage(CodeSnippetLanguage.r))
  setDefault(codeParameter ->
    """evaluate <- function(dataframe){
      |    n <- nrow(dataframe)
      |    sq.error.column <- (dataframe$label - dataframe$prediction) ^ 2
      |    sq.error.sum.column <- sum(sq.error.column)
      |    sq.error.sum <- as.data.frame(agg(dataframe, sq.error.sum.column))
      |    rmse <- sqrt(sq.error.sum / n)
      |    return(rmse)
      |}""".stripMargin
  )

  override def runCode(context: ExecutionContext, code: String): Result =
    context.customCodeExecutor.runR(code)

  override def isValid(context: ExecutionContext, code: String): Boolean =
    context.customCodeExecutor.isRValid(code)

  // Creating a dataframe is a workaround. Currently we can pass to jvm DataFrames only.
  // TODO DS-3695 Fix a metric value - dataframe workaround.
  override def getComposedCode(userCode: String): String = {
    s"""
       |$userCode
       |
       |transform <- function(dataframe) {
       |    result <- evaluate(dataframe)
       |    numeric.result <- as.numeric(result)
       |    if (is.na(numeric.result)) {
       |      stop("Invalid result of evaluate function: value '",
       |       result, "' cannot be converted to float.")
       |    }
       |
       |    result.df <- createDataFrame(as.data.frame(numeric.result))
       |    return(result.df)
       |}
      """.stripMargin
  }
}

