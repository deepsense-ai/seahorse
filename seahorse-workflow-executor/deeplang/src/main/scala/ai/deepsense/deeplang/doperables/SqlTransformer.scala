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

import org.apache.spark.sql
import org.apache.spark.sql.types.StructType

import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperations.exceptions.SqlExpressionException
import ai.deepsense.deeplang.inference.{SqlInferenceWarning, SqlSchemaInferrer}
import ai.deepsense.deeplang.params.{CodeSnippetLanguage, CodeSnippetParam, Param, StringParam}
import ai.deepsense.sparkutils.SparkSQLSession
import ai.deepsense.sparkutils.SQL

class SqlTransformer extends Transformer {

  val dataFrameId = StringParam(
    name = "dataframe id",
    description = Some("An identifier that can be used in " +
      "the SQL expression to refer to the input DataFrame."))
  setDefault(dataFrameId -> "df")
  def getDataFrameId: String = $(dataFrameId)
  def setDataFrameId(value: String): this.type = set(dataFrameId, value)

  val expression = CodeSnippetParam(
    name = "expression",
    description = Some("SQL Expression to be executed on the DataFrame."),
    language = CodeSnippetLanguage(CodeSnippetLanguage.sql))
  setDefault(expression -> "SELECT * FROM df")
  def getExpression: String = $(expression)
  def setExpression(value: String): this.type = set(expression, value)

  override val params: Array[Param[_]] = Array(dataFrameId, expression)

  override protected def applyTransform(ctx: ExecutionContext, df: DataFrame): DataFrame = {
    logger.debug(s"SqlExpression(expression = '$getExpression'," +
      s" dataFrameId = '$getDataFrameId')")

    val localSparkSQLSession = ctx.sparkSQLSession.newSession()
    val localDataFrame = moveToSparkSQLSession(df.sparkDataFrame, localSparkSQLSession)

    SQL.registerTempTable(localDataFrame, getDataFrameId)
    try {
      logger.debug(s"Table '$dataFrameId' registered. Executing the expression")
      val sqlResult = moveToSparkSQLSession(localSparkSQLSession.sql(getExpression), ctx.sparkSQLSession)
      DataFrame.fromSparkDataFrame(sqlResult)
    } finally {
      logger.debug("Unregistering the temporary table " + getDataFrameId)
      localSparkSQLSession.dropTempTable(getDataFrameId)
    }
  }

  override protected def applyTransformSchema(schema: StructType): Option[StructType] = {
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

  private def moveToSparkSQLSession(df: sql.DataFrame, destinationSession: SparkSQLSession): sql.DataFrame =
    destinationSession.createDataFrame(df.rdd, df.schema)
}
