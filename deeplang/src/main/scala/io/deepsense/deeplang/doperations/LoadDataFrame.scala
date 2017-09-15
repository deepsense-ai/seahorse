/**
 * Copyright 2015, CodiLime Inc.
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
        DataFrame.loadFromFs(context))(
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
