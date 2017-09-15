/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import io.deepsense.deeplang.doperables.DOperableLoader
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters.{AcceptAllRegexValidator, ParametersSchema, StringParameter}
import io.deepsense.deeplang.{DOperation, DOperation0To1, ExecutionContext}
import io.deepsense.models.entities.Entity

/**
 * Operation which is able to load DataFrame and deserialize it.
 */
case class LoadDataFrame() extends DOperation0To1[DataFrame] {
  override val id: DOperation.Id = "2aa22df2-e28b-11e4-8a00-1681e6b88ec1"

  override val parameters = ParametersSchema(LoadDataFrame.idParam -> StringParameter(
    "unique id of the Dataframe", None, required = true, validator = new AcceptAllRegexValidator))

  override val name: String = "Load DataFrame"

  override protected def _execute(context: ExecutionContext)(): DataFrame = {
    val idParameter = parameters.getStringParameter(LoadDataFrame.idParam)
    DOperableLoader.load(
      context.entityStorageClient)(
      DataFrame.loadFromHdfs(context))(
      context.tenantId,
      idParameter.value.get)
  }
}

object LoadDataFrame {
  val idParam = "id"

  def apply(id: String): LoadDataFrame = {
    val loadDF = new LoadDataFrame
    loadDF.parameters.getStringParameter(LoadDataFrame.idParam).value = Some(id)
    loadDF
  }
}
