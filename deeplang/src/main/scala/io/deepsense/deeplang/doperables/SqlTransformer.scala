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

package io.deepsense.deeplang.doperables

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.exceptions.SqlExpressionException
import io.deepsense.deeplang.inference.{SqlInferenceWarning, SqlSchemaInferrer}
import io.deepsense.deeplang.params.{CodeSnippetLanguage, CodeSnippetParam, Param, StringParam}
import org.apache.spark.sql
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.SparkSession

class SqlTransformer extends Transformer {

  val dataFrameId = StringParam(
    name = "dataframe id",
    description = "An identifier that can be used in " +
      "the SQL expression to refer to the input DataFrame.")
  setDefault(dataFrameId -> "df")
  def getDataFrameId: String = $(dataFrameId)
  def setDataFrameId(value: String): this.type = set(dataFrameId, value)

  val expression = CodeSnippetParam(
    name = "expression",
    description = "SQL Expression to be executed on the DataFrame.",
    language = CodeSnippetLanguage(CodeSnippetLanguage.sql))
  setDefault(expression -> "SELECT * FROM df")
  def getExpression: String = $(expression)
  def setExpression(value: String): this.type = set(expression, value)

  override val params: Array[Param[_]] = Array(dataFrameId, expression)

  override private[deeplang] def _transform(ctx: ExecutionContext, df: DataFrame): DataFrame = {
    logger.debug(s"SqlExpression(expression = '$getExpression'," +
      s" dataFrameId = '$getDataFrameId')")

    val localSparkSession = ctx.sparkSession.newSession()
    val localDataFrame = moveToSparkSession(df.sparkDataFrame, localSparkSession)

    localDataFrame.createOrReplaceTempView(getDataFrameId)
    try {
      logger.debug(s"Table '$dataFrameId' registered. Executing the expression")
      val sqlResult = moveToSparkSession(localSparkSession.sql(getExpression), ctx.sparkSession)
      DataFrame.fromSparkDataFrame(sqlResult)
    } finally {
      logger.debug("Unregistering the temporary table" + getDataFrameId)
      localSparkSession.catalog.dropTempView(getDataFrameId)
    }
  }

  override private[deeplang] def _transformSchema(schema: StructType): Option[StructType] = {
    val (resultSchema, warnings) =
      new SqlSchemaInferrer().inferSchema(getExpression, (getDataFrameId, schema))
    // We throw/log as there is no way to pass warnings further at this point.
    warnings.warnings.foreach {
      case SqlInferenceWarning(sqlExpression, warningText) =>
        throw SqlExpressionException(sqlExpression, warningText)
      case other => logger.warn(s"Inference warning not reported: ${other.message}")
    }
    Some(resultSchema)
  }

  private def moveToSparkSession(df: sql.DataFrame, destinationCtx: SparkSession): sql.DataFrame =
    destinationCtx.createDataFrame(df.rdd, df.schema)
}
