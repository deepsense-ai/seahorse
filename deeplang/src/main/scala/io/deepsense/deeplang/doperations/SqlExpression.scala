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
import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameBuilder}
import io.deepsense.deeplang.parameters.{AcceptAllRegexValidator, ParametersSchema, StringParameter}
import io.deepsense.deeplang.{DOperation1To1, ExecutionContext}

case class SqlExpression() extends DOperation1To1[DataFrame, DataFrame] {
  @transient
  override lazy val tTagTO_0 = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTI_0 = ru.typeTag[DataFrame]

  val dataFrameIdParameter = StringParameter(
    "An identifier that can be used in the SQL expression to refer to the input " +
      "DataFrame. The value has to be unique in the workflow.",
    None,
    new AcceptAllRegexValidator(),
    _value = None
  )

  val expressionParameter = StringParameter(
    "SQL Expression to be executed on the DataFrame",
    None,
    new AcceptAllRegexValidator(),
    _value = None)

  override val parameters: ParametersSchema = ParametersSchema(
    SqlExpression.dataFrameIdParameterName -> dataFrameIdParameter,
    SqlExpression.expressionParameterName -> expressionParameter
  )
  override val name: String = "SQL Expression"
  override val id: Id = "530e1420-7fbe-416b-b685-6c1e0f1137fc"

  override protected def _execute(context: ExecutionContext)(t0: DataFrame): DataFrame = {
    logger.debug(s"SqlExpression(expression = '${expressionParameter.value}'," +
      s" dataFrameId = '${dataFrameIdParameter.value}')")
    t0.sparkDataFrame.registerTempTable(dataFrameId)
    try {
      logger.debug(s"Table '$dataFrameId' registered. Executing the expression")
      val sqlResult = context.sqlContext.sql(expression)
      DataFrameBuilder(context.sqlContext)
        .buildDataFrame(sqlResult)
    } finally {
      logger.debug("Unregistering the temporary table" + dataFrameIdParameter.value)
      context.sqlContext.dropTempTable(dataFrameId)
    }
  }

  def dataFrameId: String = dataFrameIdParameter.value
  def expression: String = expressionParameter.value
}

object SqlExpression {
  val expressionParameterName = "expression"
  val dataFrameIdParameterName = "dataframe id"

  def apply(sqlExpression: String, dataFrameId: String): SqlExpression = {
    val operation = SqlExpression()
    operation.expressionParameter.value = sqlExpression
    operation.dataFrameIdParameter.value = dataFrameId
    operation
  }
}

