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

import io.deepsense.deeplang.CustomOperationExecutor._

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.exceptions.CustomOperationExecutionException
import io.deepsense.deeplang.params.{CodeSnippetLanguage, CodeSnippetParam}

class PythonTransformer extends Transformer {
  import io.deepsense.deeplang.doperations.PythonTransformation._

  val codeParameter = CodeSnippetParam(
    name = "code",
    description = "Operation source code.",
    language = CodeSnippetLanguage(CodeSnippetLanguage.python)
  )
  setDefault(codeParameter -> "def transform(dataframe):\n    return dataframe")

  override val params = declareParams(codeParameter)

  override private[deeplang] def _transform(ctx: ExecutionContext, df: DataFrame): DataFrame = {
    val code = $(codeParameter)

    if (!ctx.pythonCodeExecutor.isValid(code)) {
      throw CustomOperationExecutionException("Code validation failed")
    }



    ctx.dataFrameStorage.withInputDataFrame(InputPortNumber, df.sparkDataFrame) {
      ctx.pythonCodeExecutor.run(code) match {
        case Left(error) =>
          throw CustomOperationExecutionException(s"Execution exception:\n\n$error")

        case Right(_) =>
          val sparkDataFrame =
            ctx.dataFrameStorage.getOutputDataFrame(OutputPortNumber).getOrElse {
              throw CustomOperationExecutionException(
                "Operation finished successfully, but did not produce a DataFrame.")
            }

          DataFrame.fromSparkDataFrame(sparkDataFrame)
      }
    }
  }
}
