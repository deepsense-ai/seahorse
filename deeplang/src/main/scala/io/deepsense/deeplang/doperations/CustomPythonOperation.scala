/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.deeplang.doperations

import scala.reflect.runtime.{universe => ru}

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.exceptions.CustomOperationExecutionException
import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang.{DOperation1To1, ExecutionContext}

case class CustomPythonOperation()
  extends DOperation1To1[DataFrame, DataFrame] {

  override val id: Id = "a721fe2a-5d7f-44b3-a1e7-aade16252ead"

  override val name: String = "Custom Python operation"

  val codeParameter = CodeSnippetParameter(
    description = "Operation source code",
    default = Some("def transform(dataframe):\n  return dataframe"),
    language = CodeSnippetLanguage(CodeSnippetLanguage.Python)
  )
  override val parameters = ParametersSchema("code" -> codeParameter)

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): DataFrame = {
    val code = codeParameter.value

    if (!context.pythonCodeExecutor.validate(code)) {
      throw CustomOperationExecutionException("Code validation failed")
    }

    context.dataFrameStorage.setInputDataFrame(dataFrame.sparkDataFrame)
    context.pythonCodeExecutor.run(code) match {
      case Left(error) =>
        throw CustomOperationExecutionException(s"Execution exception:\n\n$error")

      case Right(_) =>
        val sparkDataFrame = context.dataFrameStorage.getOutputDataFrame().getOrElse {
          throw CustomOperationExecutionException(
            "Operation finished successfully, but did not produce a DataFrame.")
        }

        context.dataFrameBuilder.buildDataFrame(sparkDataFrame)
    }
  }

  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]

  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}
