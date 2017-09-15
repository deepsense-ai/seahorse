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

package ai.deepsense.seahorse.datasource.server

import java.util.UUID

import ai.deepsense.seahorse.datasource.model.Visibility.Visibility
import ai.deepsense.seahorse.datasource.model._

object TestData {

  def someDatasources(visibility: Option[Visibility] = None) = Seq(
    someJdbcDatasource(visibility),
    someLibraryCsvDatasource(visibility)
  )

  def someDatasource(visibility: Option[Visibility] = None) = someJdbcDatasource(visibility)

  def someJdbcDatasource(visibility: Option[Visibility]) = DatasourceParams(
    name = "some-name" + UUID.randomUUID(),
    downloadUri = None,
    visibility = visibility.getOrElse(Visibility.privateVisibility),
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

  def someLibraryCsvDatasource(visibility: Option[Visibility]) = DatasourceParams(
    name = "some-name" + UUID.randomUUID(),
    downloadUri = None,
    visibility = visibility.getOrElse(Visibility.publicVisibility),
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
