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

import ai.deepsense.commons.service.api.CommonApiExceptions._
import ai.deepsense.seahorse.datasource.db.schema.DatasourcesSchema.DatasourceDB
import ai.deepsense.seahorse.datasource.model.FileFormat.FileFormat
import ai.deepsense.seahorse.datasource.model._

object DatasourceApiFromDb {

  def apply(currentUserId: UUID, datasource: DatasourceDB): Validation[ApiException, Datasource] = {
    for {
      jdbcParams <- validateJdbcParams(datasource)
      externalFileParams <- validateExternalFileParams(datasource)
      libraryFileParams <- validateLibraryFileParams(datasource)
      hdfsParams <- validateHdfsParams(datasource)
      googleSpreadsheetParams <- validateGoogleSpreadsheetParams(datasource)
    } yield Datasource(
      id = datasource.generalParameters.id,
      creationDateTime = datasource.generalParameters.creationDateTime,
      accessLevel = if (currentUserId == datasource.generalParameters.ownerId) {
        AccessLevel.writeRead
      } else {
        AccessLevel.read
      },
      ownerId = datasource.generalParameters.ownerId,
      ownerName = datasource.generalParameters.ownerName,
      params = DatasourceParams(
        name = datasource.generalParameters.name,
        downloadUri = datasource.generalParameters.downloadUri,
        datasourceType = datasource.generalParameters.datasourceType,
        visibility = datasource.generalParameters.visibility,
        jdbcParams = jdbcParams,
        externalFileParams = externalFileParams,
        hdfsParams = hdfsParams,
        libraryFileParams = libraryFileParams,
        googleSpreadsheetParams = googleSpreadsheetParams
      )
    )
  }

  private def validateJdbcParams(datasource: DatasourceDB) = {
    if (datasource.generalParameters.datasourceType == DatasourceType.jdbc) {
      for {
        url <- validateDefined("jdbcUrl", datasource.jdbcParameters.jdbcUrl)
        driver <- validateDefined("jdbcDriver", datasource.jdbcParameters.jdbcDriver)
      } yield Some(JdbcParams(
        url,
        driver,
        datasource.jdbcParameters.jdbcQuery,
        datasource.jdbcParameters.jdbcTable))
    } else {
      None.success
    }
  }

  private def validateExternalFileParams(datasource: DatasourceDB) = {
    if (datasource.generalParameters.datasourceType == DatasourceType.externalFile) {
      for {
        url <- validateDefined("externalFileUrl", datasource.fileParameters.externalFileUrl)
        fileFormat <- validateDefined("fileFormat", datasource.fileParameters.fileFormat)
        csvFileFormatParams <- validateCsvFileFormatParams(datasource, fileFormat)
      } yield Some(ExternalFileParams(url, fileFormat, csvFileFormatParams))
    } else {
      None.success
    }
  }

  private def validateLibraryFileParams(datasource: DatasourceDB) = {
    if (datasource.generalParameters.datasourceType == DatasourceType.libraryFile) {
      for {
        url <- validateDefined("libraryPath", datasource.fileParameters.libraryPath)
        fileFormat <- validateDefined("fileFormat", datasource.fileParameters.fileFormat)
        csvFileFormatParams <- validateCsvFileFormatParams(datasource, fileFormat)
      } yield Some(LibraryFileParams(url, fileFormat, csvFileFormatParams))
    } else {
      None.success
    }
  }

  private def validateCsvFileFormatParams(datasource: DatasourceDB, fileFormat: FileFormat) = {
    if (fileFormat == FileFormat.csv) {
      for {
        fileCsvIncludeHeader <- validateDefined("fileCsvIncludeHeader", datasource.fileParameters.fileCsvIncludeHeader)
        fileCsvConvert01ToBoolean <- validateDefined(
          "fileCsvConvert01ToBoolean",
          datasource.fileParameters.fileCsvConvert01ToBoolean)
        fileCsvSeparatorType <- validateDefined(
          "fileCsvSeparatorType",
          datasource.fileParameters.fileCsvSeparatorType)
      } yield Some(CsvFileFormatParams(
        fileCsvIncludeHeader,
        fileCsvConvert01ToBoolean,
        fileCsvSeparatorType,
        datasource.fileParameters.fileCsvCustomSeparator))
    } else {
      None.success
    }
  }

  private def validateHdfsParams(datasource: DatasourceDB) = {
    if (datasource.generalParameters.datasourceType == DatasourceType.hdfs) {
      for {
        path <- validateDefined("hdfsPath", datasource.fileParameters.hdfsPath)
        fileFormat <- validateDefined("fileFormat", datasource.fileParameters.fileFormat)
        csvFileFormatParams <- validateCsvFileFormatParams(datasource, fileFormat)
      } yield Some(HdfsParams(path, fileFormat, csvFileFormatParams))
    } else {
      None.success
    }
  }

  private def validateGoogleSpreadsheetParams(datasource: DatasourceDB) = {
    if (datasource.generalParameters.datasourceType == DatasourceType.googleSpreadsheet) {
      for {
        googleSpreadsheetId <- validateDefined("googleSpreadsheetId", datasource.googleParameters.googleSpreadsheetId)
        googleServiceAccountCredentials <- validateDefined("googleServiceAccountCredentials",
          datasource.googleParameters.googleServiceAccountCredentials
        )
        includeHeader <- validateDefined("includeHeader", datasource.googleParameters.googleSpreadsheetIncludeHeader)
        convert01ToBoolean <- validateDefined("convert01ToBoolean",
          datasource.googleParameters.googleSpreadsheetConvert01ToBoolean
        )
      } yield Some(GoogleSpreadsheetParams(googleSpreadsheetId, googleServiceAccountCredentials,
        includeHeader, convert01ToBoolean
      ))
    } else {
      None.success
    }
  }

}
