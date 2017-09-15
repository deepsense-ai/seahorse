/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.dataframe.DataFrame
import io.deepsense.deeplang.parameters.{AcceptAllRegexValidator, ParametersSchema, StringParameter}
import io.deepsense.deeplang.{DOperation1To0, ExecutionContext}

/**
 * Operation which is able to serialize DataFrame and write it.
 */
class WriteDataFrame extends DOperation1To0[DataFrame] {
  parameters = ParametersSchema("path" -> StringParameter(
    "path to dataframe", None, required = true, validator = new AcceptAllRegexValidator))

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): Unit = {
    val pathParameter = parameters.getStringParameter("path")
    dataFrame.save(pathParameter.value.get)
  }
}
