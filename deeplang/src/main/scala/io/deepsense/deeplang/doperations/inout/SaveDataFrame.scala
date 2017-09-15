/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.deeplang.doperations.inout

import scala.reflect.runtime.{universe => ru}

import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameMetadata}
import io.deepsense.deeplang.doperables.{DOperableSaver, Report}
import io.deepsense.deeplang.doperations.OldOperation
import io.deepsense.deeplang.parameters.{AcceptAllRegexValidator, ParametersSchema, StringParameter}
import io.deepsense.deeplang.{DOperation, DOperation1To0, ExecutionContext}
import io.deepsense.entitystorage.UniqueFilenameUtil
import io.deepsense.models.entities.{CreateEntityRequest, DataObjectReference}

/**
 * Operation which is able to serialize DataFrame and save it.
 */
case class SaveDataFrame() extends DOperation1To0[DataFrame] with OldOperation {

  override val parameters = ParametersSchema(
    SaveDataFrame.nameParam -> StringParameter(
      "user friendly name", None, validator = new AcceptAllRegexValidator),
    SaveDataFrame.descriptionParam -> StringParameter(
      "description for DataFrame", None, validator = new AcceptAllRegexValidator))

  override val id: DOperation.Id = "58025c36-e28b-11e4-8a00-1681e6b88ec1"

  override val name: String = "Save DataFrame"

  override protected def _execute(context: ExecutionContext)(dataFrame: DataFrame): Unit = {
    val uniqueFilename: String = getUniqueFileName(context)
    DOperableSaver.saveDOperableWithEntityStorageRegistration(
      context)(
      dataFrame,
      inputEntity(context, uniqueFilename, dataFrame.report(context), dataFrame.metadata.get))
  }

  private def inputEntity(
      context: ExecutionContext,
      uniqueFilename: String,
      dataFrameReport: Report,
      metadata: DataFrameMetadata): CreateEntityRequest = {
    val name = parameters.getStringParameter(SaveDataFrame.nameParam).value
    val description = parameters.getStringParameter(SaveDataFrame.descriptionParam).value
    CreateEntityRequest(
      context.tenantId,
      name,
      description,
      dClass = DataFrame.getClass.toString,  // TODO https://codilime.atlassian.net/browse/DS-869
      dataReference = Some(DataObjectReference(
        uniqueFilename,
        metadata.serializeToJson.compactPrint)),
      report = dataFrameReport.toDataObjectReport,
      saved = true)
  }

  private def getUniqueFileName(context: ExecutionContext): String =
    context.uniqueFsFileName(UniqueFilenameUtil.DataFrameEntityCategory)

  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}

object SaveDataFrame {
  val nameParam = "name"
  val descriptionParam = "description"

  def apply(name: String, description: String = ""): SaveDataFrame = {
    val saveDataFrame = new SaveDataFrame
    saveDataFrame.parameters.getStringParameter(nameParam).value = name
    saveDataFrame.parameters.getStringParameter(descriptionParam).value = description
    saveDataFrame
  }
}
