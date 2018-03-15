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

import org.apache.spark.sql.types.StructType

import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.params.{CodeSnippetParam, CodeSnippetLanguage, Param}
import ai.deepsense.sparkutils.SQL

class RowsFilterer extends Transformer {

  val condition = CodeSnippetParam(
    name = "condition",
    description = Some("Condition used to filter rows. " +
      "Only rows that satisfy condition will remain in DataFrame. Use SQL syntax."),
    language = CodeSnippetLanguage(CodeSnippetLanguage.sql)
  )

  def getCondition: String = $(condition)
  def setCondition(value: String): this.type = set(condition, value)

  override val params: Array[Param[_]] = Array(condition)

  override protected def applyTransform(ctx: ExecutionContext, df: DataFrame): DataFrame = {
    val uniqueDataFrameId = "row_filterer_" + java.util.UUID.randomUUID.toString.replace('-', '_')
    val resultantExpression = s"SELECT * FROM $uniqueDataFrameId WHERE $getCondition"
    logger.debug(s"RowsFilterer(expression = 'resultantExpression'," +
      s" uniqueDataFrameId = '$uniqueDataFrameId')")

    SQL.registerTempTable(df.sparkDataFrame, uniqueDataFrameId)
    try {
      logger.debug(s"Table '$uniqueDataFrameId' registered. Executing the expression")
      val sqlResult = SQL.sparkSQLSession(df.sparkDataFrame).sql(resultantExpression)
      DataFrame.fromSparkDataFrame(sqlResult)
    } finally {
      logger.debug(s"Unregistering the temporary table '$uniqueDataFrameId'")
      SQL.sparkSQLSession(df.sparkDataFrame).dropTempTable(uniqueDataFrameId)
    }
  }

  override protected def applyTransformSchema(schema: StructType): Option[StructType] =
    Some(schema)
}
