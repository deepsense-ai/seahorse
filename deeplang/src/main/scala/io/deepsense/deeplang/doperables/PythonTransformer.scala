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

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.OperationExecutionDispatcher.Result
import io.deepsense.deeplang.params.{CodeSnippetLanguage, CodeSnippetParam}

class PythonTransformer extends CustomCodeTransformer {

  override lazy val codeParameter = CodeSnippetParam(
    name = "code",
    description = None,
    language = CodeSnippetLanguage(CodeSnippetLanguage.python)
  )
  setDefault(codeParameter -> "def transform(dataframe):\n    return dataframe")

  override def isValid(context: ExecutionContext, code: String): Boolean =
    context.customCodeExecutor.isPythonValid(code)

  override def runCode(context: ExecutionContext, code: String): Result =
    context.customCodeExecutor.runPython(code)
}
