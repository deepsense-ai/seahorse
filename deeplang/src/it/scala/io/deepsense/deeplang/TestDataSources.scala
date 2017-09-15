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

package io.deepsense.deeplang

import java.util.UUID

import org.joda.time.DateTime

import io.deepsense.api.datasourcemanager.model._
import io.deepsense.commons.rest.client.datasources.DatasourceInMemoryClient
import io.deepsense.deeplang.doperations.inout.InputFileFormatChoice
import io.deepsense.deeplang.doperations.readwritedataframe.FileScheme

trait TestDataSources {
  self: TestFiles =>

  lazy val datasourceClient = new DatasourceInMemoryClient(
    someDatasources
  )

  lazy val someDatasources = List(someJsonDatasource, someCsvDatasource)

  lazy val someJsonDatasource = {
    val externalFileUrl = testFile(
      new InputFileFormatChoice.Json(),
      FileScheme.HTTPS
    )

    new Datasource()
      .id(UUID.randomUUID().toString)
      .accessLevel(AccessLevel.READ)
      .creationDateTime(new DateTime)
      .ownerId(UUID.randomUUID().toString)
      .ownerName("SomeOwner")
      .params(new DatasourceParams()
        .name("test-external-json-file")
        .datasourceType(DatasourceType.EXTERNALFILE)
        .externalFileParams(new ExternalFileParams()
          .fileFormat(FileFormat.JSON)
          .url(externalFileUrl)
        )
      )
  }

  lazy val someCsvDatasource = {
    val externalFileUrl = testFile(
      new InputFileFormatChoice.Csv(),
      FileScheme.HTTPS
    )

    new Datasource()
      .id(UUID.randomUUID().toString)
      .accessLevel(AccessLevel.READ)
      .creationDateTime(new DateTime)
      .ownerId(UUID.randomUUID().toString)
      .ownerName("SomeOwner")
      .params(new DatasourceParams()
        .name("test-external-csv-file")
        .datasourceType(DatasourceType.EXTERNALFILE)
        .externalFileParams(new ExternalFileParams()
          .fileFormat(FileFormat.CSV)
          .url(externalFileUrl)
          .csvFileFormatParams(new CsvFileFormatParams()
            .separatorType(CsvSeparatorType.COMMA)
            .convert01ToBoolean(true)
            .includeHeader(true)
          )
        )
      )
  }

}
