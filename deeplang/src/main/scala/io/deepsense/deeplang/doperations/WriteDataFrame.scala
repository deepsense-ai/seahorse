/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 */

package io.deepsense.deeplang.doperations

import spray.json._

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.{DOperableSaver, Report}
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
    val uniqueFilename: String = getUniqueFileName(context)
    DOperableSaver.saveDOperableWithEntityStorageRegistration(
      context)(
      dataFrame,
      inputEntity(context, uniqueFilename, dataFrame.report))
  }

  private def inputEntity(
      context: ExecutionContext,
      uniqueFilename: String,
      dataFrameReport: Report): InputEntity = {
    val name = parameters.getStringParameter(WriteDataFrame.nameParam).value.get
    val description = parameters.getStringParameter(WriteDataFrame.descriptionParam).value.get
    import io.deepsense.reportlib.model.ReportJsonProtocol._
    InputEntity(
      context.tenantId,
      name,
      description,
      "DataFrame",
      Some(DataObjectReference(uniqueFilename)),
      Some(DataObjectReport(dataFrameReport.content.toJson.prettyPrint)),
      saved = true)
  }

  private def getUniqueFileName(context: ExecutionContext): String = {
    UniqueFilenameUtil.getUniqueHdfsFilename(
      context.tenantId,
      UniqueFilenameUtil.DataFrameEntityCategory)
  }
}

object WriteDataFrame {
  val nameParam = "name"
  val descriptionParam = "description"
}
