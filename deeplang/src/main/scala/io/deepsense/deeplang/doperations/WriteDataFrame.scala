/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.dataframe.DataFrame
import io.deepsense.deeplang.parameters.{AcceptAllRegexValidator, ParametersSchema, StringParameter}
import io.deepsense.deeplang.{DOperation, DOperation1To0, ExecutionContext}

/**
 * Operation which is able to serialize DataFrame and write it.
 */
class WriteDataFrame extends DOperation1To0[DataFrame] {

  override val parameters = ParametersSchema("path" -> StringParameter(
    "path to dataframe", None, required = true, validator = new AcceptAllRegexValidator))

  override val id: DOperation.Id = "58025c36-e28b-11e4-8a00-1681e6b88ec1"

  override val name: String = "Write DataFrame"

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): Unit = {
    val pathParameter = parameters.getStringParameter("path")
    dataFrame.save(pathParameter.value.get)
  }
}
