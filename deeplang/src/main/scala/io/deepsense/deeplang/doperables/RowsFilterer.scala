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
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.params.{CodeSnippetParam, CodeSnippetLanguage, Param}

class RowsFilterer extends Transformer {

  val condition = CodeSnippetParam(
    name = "condition",
    description = "Condition used to filter rows. " +
      "Only rows that satisfy condition will remain in DataFrame. Use SQL syntax.",
    language = CodeSnippetLanguage(CodeSnippetLanguage.sql)
  )

  def getCondition: String = $(condition)
  def setCondition(value: String): this.type = set(condition, value)

  override val params: Array[Param[_]] = declareParams(condition)

  override private[deeplang] def _transform(ctx: ExecutionContext, df: DataFrame): DataFrame = {
    val uniqueDataFrameId = java.util.UUID.randomUUID.toString.replace('-', '_')
    val resultantExpression = s"SELECT * FROM $uniqueDataFrameId WHERE $getCondition"
    logger.debug(s"RowsFilterer(expression = 'resultantExpression'," +
      s" uniqueDataFrameId = '$uniqueDataFrameId')")

    df.sparkDataFrame.registerTempTable(uniqueDataFrameId)
    try {
      logger.debug(s"Table '$uniqueDataFrameId' registered. Executing the expression")
      val sqlResult = df.sparkDataFrame.sqlContext.sql(resultantExpression)
      DataFrame.fromSparkDataFrame(sqlResult)
    } finally {
      logger.debug(s"Unregistering the temporary table '$uniqueDataFrameId'")
      df.sparkDataFrame.sqlContext.dropTempTable(uniqueDataFrameId)
    }
  }
}
