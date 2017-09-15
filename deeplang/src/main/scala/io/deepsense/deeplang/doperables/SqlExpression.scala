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
import io.deepsense.deeplang.params.{Param, StringParam}

class SqlExpression extends Transformer {
  val dataFrameId = StringParam(
    name = "dataframe id",
    description = "An identifier that can be used in the SQL expression to refer to the input " +
      "DataFrame. The value has to be unique in the workflow.")

  def getDataFrameId: String = $(dataFrameId)
  def setDataFrameId(value: String): this.type = set(dataFrameId, value)

  val expression = StringParam(
    name = "expression",
    description = "SQL Expression to be executed on the DataFrame.")

  def getExpression: String = $(expression)
  def setExpression(value: String): this.type = set(expression, value)

  override val params: Array[Param[_]] = declareParams(dataFrameId, expression)

  override private[deeplang] def _transform(ctx: ExecutionContext, df: DataFrame): DataFrame = {
    logger.debug(s"SqlExpression(expression = '$getExpression'," +
      s" dataFrameId = '$getDataFrameId')")
    df.sparkDataFrame.registerTempTable(getDataFrameId)
    try {
      logger.debug(s"Table '$dataFrameId' registered. Executing the expression")
      val sqlResult = ctx.sqlContext.sql(getExpression)
      DataFrame.fromSparkDataFrame(sqlResult)
    } finally {
      logger.debug("Unregistering the temporary table" + getDataFrameId)
      ctx.sqlContext.dropTempTable(getDataFrameId)
    }
  }
}
