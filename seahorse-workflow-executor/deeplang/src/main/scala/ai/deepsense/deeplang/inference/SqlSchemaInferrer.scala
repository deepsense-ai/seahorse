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

package ai.deepsense.deeplang.inference

import ai.deepsense.commons.spark.sql.UserDefinedFunctions
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{AnalysisException, Row}

import ai.deepsense.sparkutils.SQL

case class SqlInferenceWarning(sqlExpression: String, warningText: String) extends
  InferenceWarning(s"Schema for SQL formula '$sqlExpression' cannot be inferred ($warningText).")

class SqlSchemaInferrer {
  def inferSchema(sqlExpression: String, inputSchemas: (String, StructType)*)
  : (StructType, InferenceWarnings) = {
    try {
    val localSpark = SQL.createEmptySparkSQLSession()
      inputSchemas.foreach { case (dataFrameId, schema) =>
        val emptyData = localSpark.sparkContext.parallelize(Seq(Row.empty))
        val emptyDf = localSpark.createDataFrame(emptyData, schema)
        SQL.registerTempTable(emptyDf, dataFrameId)
      }
      val resultSchema = localSpark.sql(sqlExpression).schema
      val warnings = if (!namesUnique(inputSchemas)) {
        InferenceWarnings(SqlInferenceWarning(sqlExpression, "DataFrame ids must be unique."))
      } else if (resultSchema.isEmpty) {
        InferenceWarnings(SqlInferenceWarning(sqlExpression, "Expression must be non-empty."))
      } else {
        InferenceWarnings.empty
      }
      (resultSchema, warnings)
    } catch {
      case e @ (_: AnalysisException | _: IllegalArgumentException) =>
        (StructType(Seq.empty),
          InferenceWarnings(SqlInferenceWarning(sqlExpression, s"Invalid Spark SQL expression: ${e.getMessage}")))
    }
  }

  private def namesUnique(inputSchemas: Seq[(String, StructType)]): Boolean = {
    val names = inputSchemas.map { case (name, _) => name}
    names.size == names.toSet.size
  }
}
