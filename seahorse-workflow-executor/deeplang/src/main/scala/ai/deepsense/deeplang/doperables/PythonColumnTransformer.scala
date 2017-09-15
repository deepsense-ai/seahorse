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

import java.util.UUID

import ai.deepsense.deeplang.OperationExecutionDispatcher._
import org.apache.spark.sql.types._
import ai.deepsense.deeplang._
import ai.deepsense.deeplang.params.{CodeSnippetLanguage, CodeSnippetParam, Param}


case class PythonColumnTransformer() extends CustomCodeColumnTransformer {

  override val codeParameter = CodeSnippetParam(
    name = "column operation code",
    description = None,
    language = CodeSnippetLanguage(CodeSnippetLanguage.python)
  )
  setDefault(codeParameter -> "def transform_value(value, column_name):\n    return value")

  override def getSpecificParams: Array[Param[_]] =
    Array(codeParameter, targetType)

  override def getComposedCode(
      userCode: String,
      inputColumn: String,
      outputColumn: String,
      targetType: DataType): String = {
    val newFieldName = UUID.randomUUID().toString.replace("-", "")
    val newFieldJson =
      s"""{"name": "$newFieldName", "type":${targetType.json}, "nullable":true, "metadata":null}"""

    s"""
      |$userCode
      |
      |from pyspark.sql.types import *
      |import json
      |
      |def transform(dataframe):
      |    new_field = StructField.fromJson(json.loads(\"\"\"$newFieldJson\"\"\"))
      |    schema = StructType(dataframe.schema.fields + [new_field])
      |    def _transform_row(row):
      |        return row + (transform_value(row['$inputColumn'], '$inputColumn'),)
      |    return spark.createDataFrame(dataframe.rdd.map(_transform_row), schema)
    """.stripMargin
  }

  override def runCode(context: ExecutionContext, code: String): Result =
    context.customCodeExecutor.runPython(code)

  override def isValid(context: ExecutionContext, code: String): Boolean =
    context.customCodeExecutor.isPythonValid(code)
}
