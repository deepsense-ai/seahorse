/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.server

import java.util.UUID

import io.deepsense.seahorse.datasource.model._

object TestData {

  def someDatasources() = Seq(
    someJdbcDatasource(),
    someJsonFileDatasource(),
    someCsvFileDatasource()
  )

  def someJdbcDatasource() = Datasource(
    id = UUID.randomUUID(),
    name = "some-name" + UUID.randomUUID(),
    downloadUri = None,
    datasourceType = DatasourceType.jdbc,
    jdbcParams = Some(JdbcParams(
      url = "jdbc://some/url",
      driver = "com.postgresql.driver",
      table = "some_table"
    )),
    fileParams = None
  )

  def someJsonFileDatasource() = Datasource(
    id = UUID.randomUUID(),
    name = "some-name" + UUID.randomUUID(),
    downloadUri = None,
    datasourceType = DatasourceType.file,
    jdbcParams = None,
    fileParams = Some(FileParams(
      path = "hdfs://some/path",
      fileScheme = FileScheme.hdfs,
      fileFormat = FileFormat.json,
      csvFileFormatParams = None
    ))
  )

  def someCsvFileDatasource() = Datasource(
    id = UUID.randomUUID(),
    name = "some-name" + UUID.randomUUID(),
    downloadUri = None,
    datasourceType = DatasourceType.file,
    jdbcParams = None,
    fileParams = Some(FileParams(
      path = "hdfs://some/path",
      fileScheme = FileScheme.hdfs,
      fileFormat = FileFormat.csv,
      csvFileFormatParams = Some(CsvFileFormatParams(
        separator = ",",
        includeHeader = true
      ))
    ))
  )

}
