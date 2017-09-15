/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.server

import java.util.UUID

import io.deepsense.seahorse.datasource.model._

object TestData {

  def someDatasources() = Seq(
    someJdbcDatasource()
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

}
