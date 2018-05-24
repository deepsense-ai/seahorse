/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperations

import java.util.UUID

import scala.reflect.runtime.{universe => ru}

import ai.deepsense.api.datasourcemanager.model._
import ai.deepsense.commons.rest.client.datasources.DatasourceClient
import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang.documentation.OperationDocumentation
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperations.WriteDatasource.WriteDatasourceParameters
import ai.deepsense.deeplang.doperations.inout.OutputStorageTypeChoice
import ai.deepsense.deeplang.doperations.readwritedatasource.FromDatasourceConverters
import ai.deepsense.deeplang.exceptions.{DeepLangException, DeepLangMultiException}
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import ai.deepsense.deeplang.params.datasource.DatasourceIdForWriteParam
import ai.deepsense.deeplang.params.{BooleanParam, Param}
import ai.deepsense.deeplang.{DKnowledge, DOperation1To0, ExecutionContext}

class WriteDatasource()
  extends DOperation1To0[DataFrame]
  with WriteDatasourceParameters
  with OperationDocumentation {

  override def since: Version = Version(1, 4, 0)

  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]

  override val id: Id = "bf082da2-a0d9-4335-a62f-9804217a1436"
  override val name: String = "Write DataFrame"
  override val description: String = "Writes data to the data source"

  override def specificParams: Array[Param[_]] = Array(datasourceId, shouldOverwrite)

  setDefault(shouldOverwrite, true)

  override def getDatasourcesIds: Set[UUID] = get(datasourceId).toSet

  def setDatasourceId(value: UUID): this.type = set(datasourceId, value)
  def setDatasourceId(value: String): this.type = setDatasourceId(UUID.fromString(value))

  private def getDatasourceId() = $(datasourceId)
  private def getShouldOverwrite() = $(shouldOverwrite)

  override def execute(dataFrame: DataFrame)(context: ExecutionContext): Unit = {
    createWriteDataFrameFromDatasource(context.inferContext.datasourceClient).execute(dataFrame)(context)
  }

  override protected def inferKnowledge
      (k0: DKnowledge[DataFrame])
      (context: InferContext): (Unit, InferenceWarnings) = {
    val writeDataFrame = createWriteDataFrameFromDatasource(context.datasourceClient)

    val parametersValidationErrors = writeDataFrame.validateParams
    if (parametersValidationErrors.nonEmpty) {
      throw new DeepLangMultiException(parametersValidationErrors)
    }
    writeDataFrame.inferKnowledge(k0)(context)
  }

  private def createWriteDataFrameFromDatasource(datasourceClient: DatasourceClient) = {
    val datasourceOpt = datasourceClient.getDatasource(getDatasourceId())
    val datasource = checkDatasourceExists(datasourceOpt)

    val storageType: OutputStorageTypeChoice = datasource.getParams.getDatasourceType match {
      case DatasourceType.JDBC =>
        val jdbcParams = datasource.getParams.getJdbcParams
        new OutputStorageTypeChoice.Jdbc()
          .setJdbcDriverClassName(jdbcParams.getDriver)
          .setJdbcTableName(jdbcParams.getTable)
          .setJdbcUrl(jdbcParams.getUrl)
          .setShouldOverwrite(getShouldOverwrite())
      case DatasourceType.GOOGLESPREADSHEET =>
        val googleSheetParams = datasource.getParams.getGoogleSpreadsheetParams
        new OutputStorageTypeChoice.GoogleSheet()
          .setGoogleSheetId(googleSheetParams.getGoogleSpreadsheetId)
          .setGoogleServiceAccountCredentials(googleSheetParams.getGoogleServiceAccountCredentials)
          .setNamesIncluded(googleSheetParams.getIncludeHeader)
          .setShouldConvertToBoolean(googleSheetParams.getConvert01ToBoolean)
      case DatasourceType.HDFS =>
        FromDatasourceConverters.OutputFileStorageType.get(datasource.getParams.getHdfsParams)
          .setShouldOverwrite(getShouldOverwrite())
      case DatasourceType.EXTERNALFILE => throw new DeepLangException("Cannot write to external file")
      case DatasourceType.LIBRARYFILE =>
        FromDatasourceConverters.OutputFileStorageType.get(datasource.getParams.getLibraryFileParams)
          .setShouldOverwrite(getShouldOverwrite())
    }

    new WriteDataFrame().setStorageType(storageType)
  }

  private def checkDatasourceExists(datasourceOpt: Option[Datasource]) = datasourceOpt match {
    case Some(datasource) => datasource
    case None => throw new DeepLangException(s"Datasource with id = ${getDatasourceId()} not found")
  }

}

object WriteDatasource {
  def apply(): WriteDatasource = new WriteDatasource

  trait WriteDatasourceParameters {
    val datasourceId = DatasourceIdForWriteParam(
      name = "data source",
      description = None)

    val shouldOverwrite = BooleanParam(
      name = "overwrite",
      description = Some("Should data be overwritten?")
    )
  }
}
