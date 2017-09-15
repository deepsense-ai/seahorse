/**
 * Copyright 2016, deepsense.io
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

import java.util.UUID

import scala.reflect.runtime.{universe => ru}

import io.deepsense.api.datasourcemanager.model._
import io.deepsense.commons.rest.client.datasources.DatasourceClient
import io.deepsense.commons.utils.Version
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.documentation.OperationDocumentation
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.ReadDatasource.ReadDataSourceParameters
import io.deepsense.deeplang.doperations.inout.InputStorageTypeChoice
import io.deepsense.deeplang.doperations.readwritedatasource.FromDatasourceConverters
import io.deepsense.deeplang.exceptions.DeepLangException
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.params.Param
import io.deepsense.deeplang.params.datasource.DatasourceIdForReadParam
import io.deepsense.deeplang.{DKnowledge, DOperation0To1, ExecutionContext}

class ReadDatasource()
  extends DOperation0To1[DataFrame]
  with ReadDataSourceParameters
  with OperationDocumentation {

  override def since: Version = Version(1, 4, 0)

  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]

  override val id: Id = "1a3b32f0-f56d-4c44-a396-29d2dfd43423"
  override val name: String = "Read Datasource"
  override val description: String = "Reads data from its source to Seahorse's memory"

  override def params: Array[Param[_]] = Array(datasourceId)

  override def getDatasourcesId: Set[UUID] = get(datasourceId).toSet

  def setDatasourceId(value: UUID): this.type = set(datasourceId, value)
  def setDatasourceId(value: String): this.type = setDatasourceId(UUID.fromString(value))

  private def getDatasourceId() = $(datasourceId)

  override def execute()(context: ExecutionContext): DataFrame = {
    createReadDataFrameFromDataSource(context.inferContext.datasourceClient).execute()(context)
  }

  override protected def inferKnowledge()(context: InferContext): (DKnowledge[DataFrame], InferenceWarnings) = {
    createReadDataFrameFromDataSource(context.datasourceClient).inferKnowledge()(context)
  }

  private def createReadDataFrameFromDataSource(datasourceClient: DatasourceClient) = {
    val datasourceOpt = datasourceClient.getDatasource(getDatasourceId())
    val datasource = checkDataSourceExists(datasourceOpt)

    // TODO Reduce similar code with WriteDatasource

    val storageType = datasource.getParams.getDatasourceType match {
      case DatasourceType.JDBC =>
        val jdbcParams = datasource.getParams.getJdbcParams

        val from = Option(jdbcParams.getQuery).map(wrapAsSubQuery)
          .orElse(Option(jdbcParams.getTable))
          .get

        new InputStorageTypeChoice.Jdbc()
          .setJdbcDriverClassName(jdbcParams.getDriver)
          .setJdbcTableName(from)
          .setJdbcUrl(jdbcParams.getUrl)

      case DatasourceType.GOOGLESPREADSHEET =>
        val googleSheetParams = datasource.getParams.getGoogleSpreadsheetParams
        new InputStorageTypeChoice.GoogleSheet()
          .setGoogleSheetId(googleSheetParams.getGoogleSpreadsheetId)
          .setGoogleServiceAccountCredentials(googleSheetParams.getGoogleServiceAccountCredentials)
          .setNamesIncluded(googleSheetParams.getIncludeHeader)
          .setShouldConvertToBoolean(googleSheetParams.getConvert01ToBoolean)
      case DatasourceType.HDFS =>
        FromDatasourceConverters.InputFileStorageType.get(datasource.getParams.getHdfsParams)
      case DatasourceType.EXTERNALFILE =>
        FromDatasourceConverters.InputFileStorageType.get(datasource.getParams.getExternalFileParams)
      case DatasourceType.LIBRARYFILE =>
        FromDatasourceConverters.InputFileStorageType.get(datasource.getParams.getLibraryFileParams)
    }

    new ReadDataFrame().setStorageType(storageType)
  }

  private def checkDataSourceExists(datasourceOpt: Option[Datasource]) = datasourceOpt match {
    case Some(datasource) => datasource
    case None => throw new DeepLangException(s"Datasource with id = ${getDatasourceId()} not found")
  }

  private def wrapAsSubQuery(query: String): String =
    s"($query) as ${UUID.randomUUID.toString.replace("-", "")}"
}

object ReadDatasource {
  def apply(): ReadDatasource = new ReadDatasource

  trait ReadDataSourceParameters {
    val datasourceId = DatasourceIdForReadParam(
      name = "datasourceId",
      description = "Id of the datasource")
  }
}
