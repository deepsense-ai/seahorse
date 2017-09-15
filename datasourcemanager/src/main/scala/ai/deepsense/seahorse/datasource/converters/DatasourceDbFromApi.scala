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

package ai.deepsense.seahorse.datasource.converters

import java.util.UUID

import scala.language.reflectiveCalls
import scalaz.Validation.FlatMap._
import scalaz._
import scalaz.syntax.validation._

import ai.deepsense.commons.service.api.CommonApiExceptions.ApiException
import ai.deepsense.seahorse.datasource.db.schema.DatasourcesSchema._
import ai.deepsense.seahorse.datasource.model.CsvSeparatorType.CsvSeparatorType
import ai.deepsense.seahorse.datasource.model.FileFormat.FileFormat
import ai.deepsense.seahorse.datasource.model._

object DatasourceDbFromApi {

  def apply(
      userId: UUID,
      userName: String,
      datasourceId: UUID,
      dsParams: DatasourceParams): Validation[ApiException, DatasourceDB] = {
    val datasourceDb = DatasourceDB(
      generalParameters = DatasourceDBGeneralParameters(
        id = datasourceId,
        ownerId = userId,
        ownerName = userName,
        name = dsParams.name,
        creationDateTime = new java.util.Date(),
        visibility = dsParams.visibility,
        downloadUri = dsParams.downloadUri,
        datasourceType = dsParams.datasourceType
      ),
      jdbcParameters = DatasourceDBJdbcParameters(
        jdbcUrl = toBeOptionallyFilledLater,
        jdbcDriver = toBeOptionallyFilledLater,
        jdbcTable = toBeOptionallyFilledLater,
        jdbcQuery = toBeOptionallyFilledLater
      ),
      fileParameters = DatasourceDBFileParameters(
        externalFileUrl = toBeOptionallyFilledLater,
        libraryPath = toBeOptionallyFilledLater,
        hdfsPath = toBeOptionallyFilledLater,
        fileFormat = toBeOptionallyFilledLater,
        fileCsvIncludeHeader = toBeOptionallyFilledLater,
        fileCsvConvert01ToBoolean = toBeOptionallyFilledLater,
        fileCsvSeparatorType = toBeOptionallyFilledLater,
        fileCsvCustomSeparator = toBeOptionallyFilledLater
      ),
      googleParameters = DatasourceDBGoogleParameters(
        googleSpreadsheetId = toBeOptionallyFilledLater,
        googleServiceAccountCredentials = toBeOptionallyFilledLater,
        googleSpreadsheetIncludeHeader = toBeOptionallyFilledLater,
        googleSpreadsheetConvert01ToBoolean = toBeOptionallyFilledLater
      )
    )
    withForDatasourceTypeSpecificParams(datasourceDb, dsParams)
  }

  private val toBeOptionallyFilledLater = None

  private def withForDatasourceTypeSpecificParams(
      datasourceDb: DatasourceDB,
      ds: DatasourceParams) = ds.datasourceType match {
    case DatasourceType.jdbc =>
      for {
        jdbcParams <- validateDefined("jdbcParams", ds.jdbcParams)
      } yield datasourceDb.copy(jdbcParameters = DatasourceDBJdbcParameters(
        jdbcUrl = Some(jdbcParams.url),
        jdbcDriver = Some(jdbcParams.driver),
        jdbcTable = jdbcParams.table,
        jdbcQuery = jdbcParams.query
      ))
    case DatasourceType.googleSpreadsheet =>
      for {
        googleSpreadsheetParams <- validateDefined("googleSpreadsheetParams", ds.googleSpreadsheetParams)
      } yield datasourceDb.copy(googleParameters = DatasourceDBGoogleParameters(
        googleSpreadsheetId = Some(googleSpreadsheetParams.googleSpreadsheetId),
        googleServiceAccountCredentials = Some(googleSpreadsheetParams.googleServiceAccountCredentials),
        googleSpreadsheetIncludeHeader = Some(googleSpreadsheetParams.includeHeader),
        googleSpreadsheetConvert01ToBoolean = Some(googleSpreadsheetParams.convert01ToBoolean)
      ))
    case DatasourceType.hdfs => for {
      hdfsParams <- validateDefined("hdfsParams", ds.hdfsParams)
      withCommonParams <- withCommonFileParams(datasourceDb, hdfsParams)
    } yield withCommonParams.copy(fileParameters = withCommonParams.fileParameters.copy(
      hdfsPath = Some(hdfsParams.hdfsPath)
    ))
    case DatasourceType.externalFile => for {
      externalFileParams <- validateDefined("externalFileParams", ds.externalFileParams)
      withCommonParams <- withCommonFileParams(datasourceDb, externalFileParams)
    } yield withCommonParams.copy(fileParameters = withCommonParams.fileParameters.copy(
      externalFileUrl = Some(externalFileParams.url)
    ))
    case DatasourceType.libraryFile => for {
      libraryFile <- validateDefined("libraryFileParams", ds.libraryFileParams)
      withFile <- withCommonFileParams(datasourceDb, libraryFile)
    } yield withFile.copy(fileParameters = withFile.fileParameters.copy(
      libraryPath = Some(libraryFile.libraryPath)
    ))
  }

  private def withCommonFileParams[T <: {
    def fileFormat : FileFormat
    def csvFileFormatParams : Option[CsvFileFormatParams]
  }](datasource: DatasourceDB, apiFileParams: T) = {
    if (apiFileParams.fileFormat == FileFormat.csv) {
      for {
        csvFileFormatParams <- validateDefined("csvFileFormatParams", apiFileParams.csvFileFormatParams)
        csvFileCustomSeparator <- validateCustomSeparator(csvFileFormatParams)
      } yield datasource.copy(fileParameters = datasource.fileParameters.copy(
        fileCsvIncludeHeader = Some(csvFileFormatParams.includeHeader),
        fileCsvConvert01ToBoolean = Some(csvFileFormatParams.convert01ToBoolean),
        fileCsvSeparatorType = Some(csvFileFormatParams.separatorType),
        fileCsvCustomSeparator = csvFileCustomSeparator
      ))
    } else {
      datasource.success
    }
  }.map(result => result.copy(fileParameters = result.fileParameters.copy(
    fileFormat = Some(apiFileParams.fileFormat)
  )))

  private def validateCustomSeparator(csvFileFormatParams: CsvFileFormatParams) =
    if (csvFileFormatParams.separatorType == CsvSeparatorType.custom) {
      for {
        customSeparatorString <- validateDefined("customSeparator", csvFileFormatParams.customSeparator)
        customSeparatorChar <- validateIsChar(customSeparatorString)
      } yield Some(customSeparatorChar)
    } else {
      None.success
    }

  private def validateIsChar(string: String) = {
    if (string.length == 1) {
      string.success
    } else {
      ApiException(
        message = s"CSV custom separator should be single character", errorCode = 400
      ).failure
    }
  }

}
