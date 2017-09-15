/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.dataframe.{DataFrameBuilder, DataFrame}
import io.deepsense.deeplang.parameters.{AcceptAllRegexValidator, RegexValidator, StringParameter, ParametersSchema}
import io.deepsense.deeplang.{ExecutionContext, DOperation0To1}

/**
 * Operation which is able to read DataFrame and deserialize it.
 */
class ReadDataFrame extends DOperation0To1[DataFrame] {
  parameters = ParametersSchema("path" -> StringParameter(
    "path to dataframe", None, required = true, validator = new AcceptAllRegexValidator))

  override protected def _execute(context: ExecutionContext)(): DataFrame = {
    val pathParameter = parameters.getStringParameter("path")

    val sqlContext = context.sqlContext
    val dataFrame = sqlContext.jsonFile(pathParameter.value.get)

    val builder = context.dataFrameBuilder
    builder.buildDataFrame(dataFrame)
  }
}
