/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.dataframe.DataFrame
import io.deepsense.deeplang.parameters.{AcceptAllRegexValidator, ParametersSchema, StringParameter}
import io.deepsense.deeplang.{DOperation0To1, ExecutionContext}

/**
 * Operation which is able to read DataFrame and deserialize it.
 */
class ReadDataFrame extends DOperation0To1[DataFrame] {
  override val parameters = ParametersSchema("path" -> StringParameter(
    "path to dataframe", None, required = true, validator = new AcceptAllRegexValidator))

  override val name: String = "Read DataFrame"

  override protected def _execute(context: ExecutionContext)(): DataFrame = {
    val pathParameter = parameters.getStringParameter("path")

    val sqlContext = context.sqlContext
    val dataFrame = sqlContext.parquetFile(pathParameter.value.get)

    val builder = context.dataFrameBuilder
    builder.buildDataFrame(dataFrame)
  }
}
