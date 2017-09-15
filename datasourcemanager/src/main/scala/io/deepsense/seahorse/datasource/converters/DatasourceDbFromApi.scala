/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.converters

import java.util.UUID

import scala.language.reflectiveCalls
import scalaz.Validation.FlatMap._
import scalaz._
import scalaz.syntax.validation._

import io.deepsense.commons.service.api.CommonApiExceptions.ApiException
import io.deepsense.seahorse.datasource.db.schema.DatasourcesSchema.DatasourceDB
import io.deepsense.seahorse.datasource.model.FileFormat.FileFormat
import io.deepsense.seahorse.datasource.model._

object DatasourceDbFromApi {

  def apply(
      userId: UUID,
      datasourceId: UUID,
      dsParams: DatasourceParams): Validation[ApiException, DatasourceDB] = {
    val datasourceDb = DatasourceDB(
      id = datasourceId,
      ownerId = userId,
      name = dsParams.name,
      visibility = dsParams.visibility,
      downloadUri = dsParams.downloadUri,
      datasourceType = dsParams.datasourceType,
      jdbcUrl = toBeOptionallyFilledLater,
      jdbcDriver = toBeOptionallyFilledLater,
      jdbcTable = toBeOptionallyFilledLater,
      jdbcQuery = toBeOptionallyFilledLater,
      externalFileUrl = toBeOptionallyFilledLater,
      libraryPath = toBeOptionallyFilledLater,
      hdfsPath = toBeOptionallyFilledLater,
      fileFormat = toBeOptionallyFilledLater,
      fileCsvIncludeHeader = toBeOptionallyFilledLater,
      fileCsvConvert01ToBoolean = toBeOptionallyFilledLater,
      fileCsvSeparatorType = toBeOptionallyFilledLater,
      fileCsvCustomSeparator = toBeOptionallyFilledLater,
      googleSpreadsheetId = toBeOptionallyFilledLater,
      googleServiceAccountCredentials = toBeOptionallyFilledLater
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
      } yield datasourceDb.copy(
        jdbcUrl = Some(jdbcParams.url),
        jdbcDriver = Some(jdbcParams.driver),
        jdbcTable = jdbcParams.table,
        jdbcQuery = jdbcParams.query
      )
    case DatasourceType.googleSpreadsheet =>
      for {
        googleSpreadsheetParams <- validateDefined("googleSpreadsheetParams", ds.googleSpreadsheetParams)
      } yield datasourceDb.copy(
        googleSpreadsheetId = Some(googleSpreadsheetParams.googleSpreadsheetId),
        googleServiceAccountCredentials = Some(googleSpreadsheetParams.googleServiceAccountCredentials)
      )
    case DatasourceType.hdfs => for {
      hdfsParams <- validateDefined("hdfsParams", ds.hdfsParams)
      withCommonParams <- withCommonFileParams(datasourceDb, hdfsParams)
    } yield withCommonParams.copy(
      hdfsPath = Some(hdfsParams.hdfsPath)
    )
    case DatasourceType.externalFile => for {
      externalFileParams <- validateDefined("externalFileParams", ds.externalFileParams)
      withCommonParams <- withCommonFileParams(datasourceDb, externalFileParams)
    } yield withCommonParams.copy(
      externalFileUrl = Some(externalFileParams.url)
    )
    case DatasourceType.libraryFile => for {
      libraryFile <- validateDefined("libraryFileParams", ds.libraryFileParams)
      withFile <- withCommonFileParams(datasourceDb, libraryFile)
    } yield withFile.copy(
      libraryPath = Some(libraryFile.libraryPath)
    )
  }

  private def withCommonFileParams[T <: {
    def fileFormat : FileFormat
    def csvFileFormatParams : Option[CsvFileFormatParams]
  }](datasource: DatasourceDB, apiFileParams: T) = {
    if (apiFileParams.fileFormat == FileFormat.csv) {
      for {
        csvFileFormatParams <- validateDefined("csvFileFormatParams", apiFileParams.csvFileFormatParams)
      } yield datasource.copy(
        fileCsvIncludeHeader = Some(csvFileFormatParams.includeHeader),
        fileCsvConvert01ToBoolean = Some(csvFileFormatParams.convert01ToBoolean),
        fileCsvSeparatorType = Some(csvFileFormatParams.separatorType),
        fileCsvCustomSeparator = csvFileFormatParams.customSeparator
      )
    } else {
      datasource.success
    }
  }.map(_.copy(fileFormat = Some(apiFileParams.fileFormat)))

}
