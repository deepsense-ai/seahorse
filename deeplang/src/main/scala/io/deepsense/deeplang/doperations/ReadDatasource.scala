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
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperations.ReadDatasource.{FileStorageType, ReadDataSourceParameters}
import io.deepsense.deeplang.doperations.inout.CsvParameters.ColumnSeparatorChoice
import io.deepsense.deeplang.doperations.inout.{InputFileFormatChoice, InputStorageTypeChoice}
import io.deepsense.deeplang.exceptions.DeepLangException
import io.deepsense.deeplang.inference.{InferContext, InferenceWarnings}
import io.deepsense.deeplang.params.{Param, StringParam}
import io.deepsense.deeplang.{DKnowledge, DOperation0To1, ExecutionContext}

class ReadDatasource() extends DOperation0To1[DataFrame] with ReadDataSourceParameters {
  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]

  override val id: Id = "1a3b32f0-f56d-4c44-a396-29d2dfd43423"
  override val name: String = "Read Datasource"
  override val description: String = "Reads data from its source to Seahorse's memory"

  override def params: Array[Param[_]] = Array(datasourceId)

  override def getDatasourcesId: Set[UUID] = Set(
    UUID.fromString(getDataSourceId())
  )

  // TODO Make custom parameter for frontend. Similar in WriteDataSource

  def setDatasourceId(value: String) = set(datasourceId, value)
  private def getDataSourceId() = $(datasourceId)

  override def execute()(context: ExecutionContext): DataFrame = {
    createReadDataFrameFromDataSource(context.inferContext.datasourceClient).execute()(context)
  }

  override protected def inferKnowledge()(context: InferContext): (DKnowledge[DataFrame], InferenceWarnings) = {
    createReadDataFrameFromDataSource(context.datasourceClient).inferKnowledge()(context)
  }

  private def createReadDataFrameFromDataSource(datasourceClient: DatasourceClient) = {
    val datasourceOpt = datasourceClient.getDatasource(UUID.fromString(getDataSourceId()))
    val datasource = checkDataSourceExists(datasourceOpt)

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
        FileStorageType.get(datasource.getParams.getHdfsParams)
      case DatasourceType.EXTERNALFILE =>
        FileStorageType.get(datasource.getParams.getExternalFileParams)
      case DatasourceType.LIBRARYFILE =>
        FileStorageType.get(datasource.getParams.getLibraryFileParams)
    }

    new ReadDataFrame().setStorageType(storageType)
  }

  private def checkDataSourceExists(datasourceOpt: Option[Datasource]) = datasourceOpt match {
    case Some(datasource) => datasource
    case None => throw new DeepLangException(s"Failed to read data source with id = ${getDataSourceId()}")
  }

  private def wrapAsSubQuery(query: String): String =
    s"($query) as ${UUID.randomUUID.toString.replace("-", "")}"
}

object ReadDatasource {
  trait ReadDataSourceParameters {

    val datasourceId = StringParam("data source", "")
  }

  trait InputStorageParams {
    def path: String
    def fileFormat: FileFormat
    def csvFileFormatParams: CsvFileFormatParams
  }

  implicit def hdfsParamsToInputStorageParams(params: HdfsParams): InputStorageParams =
    new InputStorageParams {
      override def path: String = params.getHdfsPath
      override def fileFormat: FileFormat = params.getFileFormat
      override def csvFileFormatParams: CsvFileFormatParams = params.getCsvFileFormatParams
    }

  implicit def externalFileParamsToInputStorageParams(params: ExternalFileParams): InputStorageParams =
    new InputStorageParams {
      override def path: String = params.getUrl
      override def fileFormat: FileFormat = params.getFileFormat
      override def csvFileFormatParams: CsvFileFormatParams = params.getCsvFileFormatParams
  }

  implicit def libraryParamsToInputStorageParams(params: LibraryFileParams): InputStorageParams =
    new InputStorageParams {
      override def path: String = params.getLibraryPath
      override def fileFormat: FileFormat = params.getFileFormat
      override def csvFileFormatParams: CsvFileFormatParams = params.getCsvFileFormatParams
  }

  object FileStorageType {
    def get(inputStorageParams: InputStorageParams): InputStorageTypeChoice.File =
      new InputStorageTypeChoice.File()
        .setSourceFile(inputStorageParams.path)
        .setFileFormat(inputStorageParams.fileFormat match {
          case FileFormat.JSON => new InputFileFormatChoice.Json()
          case FileFormat.PARQUET => new InputFileFormatChoice.Parquet()
          case FileFormat.CSV => csvFormatChoice(inputStorageParams.csvFileFormatParams)
        })

    private def csvFormatChoice(csvParams: CsvFileFormatParams): InputFileFormatChoice.Csv =
      new InputFileFormatChoice.Csv()
        .setNamesIncluded(csvParams.getIncludeHeader)
        .setCsvColumnSeparator(csvParams.getSeparatorType match {
          case CsvSeparatorType.COLON => ColumnSeparatorChoice.Colon()
          case CsvSeparatorType.COMMA => ColumnSeparatorChoice.Comma()
          case CsvSeparatorType.SEMICOLON => ColumnSeparatorChoice.Semicolon()
          case CsvSeparatorType.SPACE => ColumnSeparatorChoice.Space()
          case CsvSeparatorType.TAB => ColumnSeparatorChoice.Tab()
          case CsvSeparatorType.CUSTOM =>
            ColumnSeparatorChoice.Custom()
              .setCustomColumnSeparator(csvParams.getCustomSeparator)
        })
  }
}
