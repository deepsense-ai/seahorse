/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.deeplang.doperations

import scala.concurrent.Await

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters.{AcceptAllRegexValidator, ParametersSchema, StringParameter}
import io.deepsense.deeplang.{DOperation, DOperation0To1, ExecutionContext}

/**
 * Operation which is able to read DataFrame and deserialize it.
 */
class ReadDataFrame extends DOperation0To1[DataFrame] {
  override val id: DOperation.Id = "2aa22df2-e28b-11e4-8a00-1681e6b88ec1"

  override val parameters = ParametersSchema(ReadDataFrame.idParam -> StringParameter(
    "unique id of the Dataframe", None, required = true, validator = new AcceptAllRegexValidator))

  override val name: String = "Read DataFrame"

  override protected def _execute(context: ExecutionContext)(): DataFrame = {
    val idParameter = parameters.getStringParameter(ReadDataFrame.idParam)

    val sqlContext = context.sqlContext

    import scala.concurrent.duration._
    // TODO: duration from configuration (and possibly a little longer timeout)
    implicit val timeout = 5.seconds
    val entityF = context.entityStorageClient.getEntityData(context.tenantId, idParameter.value.get)
    val entity = Await.result(entityF, timeout).get

    val dataFrame = sqlContext.parquetFile(entity.data.get.url)

    val builder = context.dataFrameBuilder
    builder.buildDataFrame(dataFrame)
  }
}

object ReadDataFrame {
  val idParam = "id"
}
