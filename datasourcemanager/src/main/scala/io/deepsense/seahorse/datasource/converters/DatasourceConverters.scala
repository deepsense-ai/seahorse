/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.converters

import java.util.UUID

import io.deepsense.seahorse.datasource.db.schema.DatasourcesSchema.DatasourceDB
import io.deepsense.seahorse.datasource.model._

object DatasourceConverters {

  def fromApi(datasource: Datasource): DatasourceDB = DatasourceDB(
    id = datasource.id,
    name = datasource.name,
    downloadUri = datasource.downloadUri,
    datasourceType = datasource.datasourceType,
    jdbcUrl = datasource.jdbcParams.map(_.url),
    jdbcDriver = datasource.jdbcParams.map(_.driver),
    jdbcTable = datasource.jdbcParams.map(_.table),
    filePath = datasource.fileParams.map(_.path),
    fileScheme = datasource.fileParams.map(_.fileScheme),
    fileFormat = datasource.fileParams.map(_.fileFormat),
    fileCsvSeparator = datasource.fileParams.flatMap(_.csvFileFormatParams).map(_.separator),
    fileCsvIncludeHeader = datasource.fileParams.flatMap(_.csvFileFormatParams).map(_.includeHeader)
  )

  def fromDb(datasource: DatasourceDB) = Datasource(
    id = datasource.id,
    name = datasource.name,
    downloadUri = datasource.downloadUri,
    datasourceType = datasource.datasourceType,
    jdbcParams = datasource.datasourceType match {
      case DatasourceType.jdbc => Some(JdbcParams(
        url = datasource.jdbcUrl.get,
        driver = datasource.jdbcDriver.get,
        table = datasource.jdbcTable.get
      ))
      case notJdbc => None
    },
    fileParams = datasource.datasourceType match {
      case DatasourceType.file => Some(FileParams(
        path = datasource.filePath.get,
        fileScheme = datasource.fileScheme.get,
        fileFormat = datasource.fileFormat.get,
        csvFileFormatParams = datasource.fileFormat.get match {
          case FileFormat.csv => Some(CsvFileFormatParams(
            separator = datasource.fileCsvSeparator.get,
            includeHeader = datasource.fileCsvIncludeHeader.get
          ))
          case notCsv => None
        }
      ))
      case notFile => None
    }
  )

}
