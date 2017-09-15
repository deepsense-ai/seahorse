/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.server

import java.util.UUID

import io.deepsense.seahorse.datasource.model._

object TestData {

  def someDatasources() = Seq(
    someJdbcDatasource(),
    someLibraryCsvDatasource()
  )

  def someDatasource() = someJdbcDatasource()

  def someJdbcDatasource() = DatasourceParams(
    name = "some-name" + UUID.randomUUID(),
    downloadUri = None,
    visibility = Visibility.privateVisibility,
    datasourceType = DatasourceType.jdbc,
    jdbcParams = Some(JdbcParams(
      url = "jdbc://some/url" + UUID.randomUUID(),
      driver = "com.postgresql.driver",
      table = Some("some_table" + UUID.randomUUID()),
      query = None
    )),
    externalFileParams = None,
    libraryFileParams = None,
    hdfsParams = None,
    googleSpreadsheetParams = None
  )

  def someLibraryCsvDatasource() = DatasourceParams(
    name = "some-name" + UUID.randomUUID(),
    downloadUri = None,
    visibility = Visibility.publicVisibility,
    datasourceType = DatasourceType.libraryFile,
    jdbcParams = None,
    externalFileParams = None,
    libraryFileParams = Some(LibraryFileParams(
      "some_path",
      fileFormat = FileFormat.csv,
      csvFileFormatParams = Some(CsvFileFormatParams(
        includeHeader = true, convert01ToBoolean = true, CsvSeparatorType.comma, None
      ))
    )),
    hdfsParams = None,
    googleSpreadsheetParams = None
  )

  def multicharSeparatorLibraryCsvDatasource() = DatasourceParams(
    name = "some-name" + UUID.randomUUID(),
    downloadUri = None,
    visibility = Visibility.publicVisibility,
    datasourceType = DatasourceType.libraryFile,
    jdbcParams = None,
    externalFileParams = None,
    libraryFileParams = Some(LibraryFileParams(
      "some_path",
      fileFormat = FileFormat.csv,
      csvFileFormatParams = Some(CsvFileFormatParams(
        includeHeader = true, convert01ToBoolean = true, CsvSeparatorType.custom, Some(",,")
      ))
    )),
    hdfsParams = None,
    googleSpreadsheetParams = None
  )

}
