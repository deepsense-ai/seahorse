/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import scala.concurrent.Await
import scala.concurrent.duration._

import io.deepsense.deeplang.DOperable.AbstractMetadata
import io.deepsense.deeplang.doperables.DOperableLoader
import io.deepsense.deeplang.doperables.dataframe.{DataFrameBuilder, DataFrameMetadata, DataFrame}
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.parameters.{AcceptAllRegexValidator, ParametersSchema, StringParameter}
import io.deepsense.deeplang._
import spray.json._

/**
 * Operation which is able to load DataFrame and deserialize it.
 */
case class LoadDataFrame() extends DOperation0To1[DataFrame] {
  override val id: DOperation.Id = "2aa22df2-e28b-11e4-8a00-1681e6b88ec1"

  val idParameter = StringParameter(
    "unique id of the Dataframe", None, required = true, validator = new AcceptAllRegexValidator)

  override val parameters = ParametersSchema(LoadDataFrame.IdParam -> idParameter)

  override val name: String = "Load DataFrame"

  override protected def _execute(context: ExecutionContext)(): DataFrame = {
    DOperableLoader.load(
      context.entityStorageClient)(
        DataFrame.loadFromHdfs(context))(
        context.tenantId,
        idParameter.value.get)
  }

  override protected def _inferFullKnowledge(
      context: InferContext)(): (DKnowledge[DataFrame], InferenceWarnings) = {
    implicit val timeout = 5.seconds
    val entityFuture = context.entityStorageClient.getEntityData(
      context.tenantId,
      idParameter.value.get)
    val entity = Await.result(entityFuture, timeout)
    val metadata = DataFrameMetadata.deserializeFromJson(
      entity.get.dataReference.metadata.parseJson)
    val df = DataFrameBuilder.buildDataFrameForInference(metadata)
    (new DKnowledge[DataFrame](df), InferenceWarnings.empty)
  }
}

object LoadDataFrame {
  val IdParam = "id"

  def apply(id: String): LoadDataFrame = {
    val loadDF = new LoadDataFrame
    loadDF.idParameter.value = Some(id)
    loadDF
  }
}
