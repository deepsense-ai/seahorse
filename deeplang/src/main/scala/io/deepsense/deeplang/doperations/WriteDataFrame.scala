/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Radoslaw Kotowski
 */

package io.deepsense.deeplang.doperations

import scala.concurrent.Await
import scala.util.Random

import io.deepsense.deeplang.dataframe.DataFrame
import io.deepsense.deeplang.parameters.{AcceptAllRegexValidator, ParametersSchema, StringParameter}
import io.deepsense.deeplang.{DOperation, DOperation1To0, ExecutionContext}
import io.deepsense.entitystorage.UniqueFilenameUtil
import io.deepsense.models.entities.{DataObjectReference, DataObjectReport, InputEntity}

/**
 * Operation which is able to serialize DataFrame and write it.
 */
class WriteDataFrame extends DOperation1To0[DataFrame] {

  override val parameters = ParametersSchema(
    WriteDataFrame.nameParam -> StringParameter(
      "user friendly name", None, required = true, validator = new AcceptAllRegexValidator),
    WriteDataFrame.descriptionParam -> StringParameter(
      "description for DataFrame", None, required = true, validator = new AcceptAllRegexValidator))

  override val id: DOperation.Id = "58025c36-e28b-11e4-8a00-1681e6b88ec1"

  override val name: String = "Write DataFrame"

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): Unit = {
    val nameParameter = parameters.getStringParameter(WriteDataFrame.nameParam)
    val descriptionParameter = parameters.getStringParameter(WriteDataFrame.descriptionParam)

    // TODO: check if ds-dev-env-master:8020 is necessary, if yes then use config string
    val uniqueFilename = "hdfs://ds-dev-env-master:8020" +
      UniqueFilenameUtil.getUniqueHdfsFilename(
        context.tenantId,
        UniqueFilenameUtil.DataFrameEntityCategory,
        // TODO: use deepsense app deployment directory
        "tests/WriteDataFrameTest")

    dataFrame.save(uniqueFilename)


    import scala.concurrent.duration._
    // TODO: duration from configuration (and possibly a little longer timeout)
    implicit val timeout = 5.seconds
    val entityF = context.entityStorageClient.createEntity(InputEntity(
      context.tenantId,
      nameParameter.value.get,
      descriptionParameter.value.get,
      "DataFrame",
      Some(DataObjectReference(uniqueFilename)),
      Some(DataObjectReport("WriteDataFrame Report Mock")),
      true))
    // TODO: be sure that this will fail if timeout expired
    Await.result(entityF, timeout)
  }
}

object WriteDataFrame {
  val nameParam = "name"
  val descriptionParam = "description"
}
