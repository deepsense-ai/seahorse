/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.converters

import java.util.UUID

import scala.language.reflectiveCalls
import scalaz.Validation.FlatMap._
import scalaz._
import scalaz.syntax.validation._

import io.deepsense.commons.service.api.CommonApiExceptions._
import io.deepsense.seahorse.datasource.db.schema.DatasourcesSchema.DatasourceDB
import io.deepsense.seahorse.datasource.model.FileFormat.FileFormat
import io.deepsense.seahorse.datasource.model._

object DatasourceApiFromDb {

  def apply(currentUserId: UUID, datasource: DatasourceDB): Validation[ApiException, Datasource] = {
    for {
      jdbcParams <- validateJdbcParams(datasource)
      externalFileParams <- validateExternalFileParams(datasource)
      libraryFileParams <- validateLibraryFileParams(datasource)
      hdfsParams <- validateHdfsParams(datasource)
      googleSpreadsheetParams <- validateGoogleSpreadsheetParams(datasource)
    } yield Datasource(
      id = datasource.id,
      accessLevel = if (currentUserId == datasource.ownerId) {
        AccessLevel.writeRead
      } else {
        AccessLevel.read
      },
      ownerId = datasource.ownerId,
      params = DatasourceParams(
        name = datasource.name,
        downloadUri = datasource.downloadUri,
        datasourceType = datasource.datasourceType,
        visibility = datasource.visibility,
        jdbcParams = jdbcParams,
        externalFileParams = externalFileParams,
        hdfsParams = hdfsParams,
        libraryFileParams = libraryFileParams,
        googleSpreadsheetParams = googleSpreadsheetParams
      )
    )
  }

  private def validateJdbcParams(datasource: DatasourceDB) = {
    if (datasource.datasourceType == DatasourceType.jdbc) {
      for {
        url <- validateDefined("jdbcUrl", datasource.jdbcUrl)
        driver <- validateDefined("jdbcDriver", datasource.jdbcDriver)
      } yield Some(JdbcParams(url, driver, datasource.jdbcQuery, datasource.jdbcTable))
    } else {
      None.success
    }
  }

  private def validateExternalFileParams(datasource: DatasourceDB) = {
    if (datasource.datasourceType == DatasourceType.externalFile) {
      for {
        url <- validateDefined("externalFileUrl", datasource.externalFileUrl)
        fileFormat <- validateDefined("fileFormat", datasource.fileFormat)
        csvFileFormatParams <- validateCsvFileFormatParams(datasource, fileFormat)
      } yield Some(ExternalFileParams(url, fileFormat, csvFileFormatParams))
    } else {
      None.success
    }
  }

  private def validateLibraryFileParams(datasource: DatasourceDB) = {
    if (datasource.datasourceType == DatasourceType.libraryFile) {
      for {
        url <- validateDefined("libraryPath", datasource.libraryPath)
        fileFormat <- validateDefined("fileFormat", datasource.fileFormat)
        csvFileFormatParams <- validateCsvFileFormatParams(datasource, fileFormat)
      } yield Some(LibraryFileParams(url, fileFormat, csvFileFormatParams))
    } else {
      None.success
    }
  }

  private def validateCsvFileFormatParams(datasource: DatasourceDB, fileFormat: FileFormat) = {
    if (fileFormat == FileFormat.csv) {
      for {
        fileCsvIncludeHeader <- validateDefined("fileCsvIncludeHeader", datasource.fileCsvIncludeHeader)
        fileCsvConvert01ToBoolean <- validateDefined("fileCsvConvert01ToBoolean", datasource.fileCsvConvert01ToBoolean)
        fileCsvSeparatorType <- validateDefined("fileCsvSeparatorType", datasource.fileCsvSeparatorType)
      } yield Some(CsvFileFormatParams(
        fileCsvIncludeHeader, fileCsvConvert01ToBoolean, fileCsvSeparatorType, datasource.fileCsvCustomSeparator
      ))
    } else {
      None.success
    }
  }

  private def validateHdfsParams(datasource: DatasourceDB) = {
    if (datasource.datasourceType == DatasourceType.hdfs) {
      for {
        path <- validateDefined("hdfsPath", datasource.hdfsPath)
        fileFormat <- validateDefined("fileFormat", datasource.fileFormat)
        csvFileFormatParams <- validateCsvFileFormatParams(datasource, fileFormat)
      } yield Some(HdfsParams(path, fileFormat, csvFileFormatParams))
    } else {
      None.success
    }
  }

  private def validateGoogleSpreadsheetParams(datasource: DatasourceDB) = {
    if (datasource.datasourceType == DatasourceType.googleSpreadsheet) {
      for {
        googleSpreadsheetId <- validateDefined("googleSpreadsheetId", datasource.googleSpreadsheetId)
        googleServiceAccountCredentials <- validateDefined("fileFormat", datasource.googleServiceAccountCredentials)
      } yield Some(GoogleSpreadsheetParams(googleSpreadsheetId, googleServiceAccountCredentials))
    } else {
      None.success
    }
  }

}
