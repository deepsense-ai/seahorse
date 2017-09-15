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

package ai.deepsense.e2etests

import java.util.UUID

import ai.deepsense.api.datasourcemanager.model._

trait TestDatasourcesInserter {

  self: SeahorseIntegrationTestDSL =>

  def insertDatasourcesForTest(): Unit = {
    for (DatasourceForTest(id, ds) <- datasourcesNeededForTests) yield {
      insertDatasource(id, ds)
    }
  }

  private def datasourcesNeededForTests = List(
    DatasourceForTest(
      uuid = "425c1536-7039-47d7-8db4-5c4e8bb9d742",
      csvFromHttp("transactions", fromS3("transactions"))),
    DatasourceForTest(
      uuid = "3f64a431-6248-48df-826c-b662b076b135",
      csvFromHttp("small state names", fromS3("StateNamesSmall"))
    )
  )

  private def csvFromHttp(name: String, url: String): DatasourceParams = {
    (new DatasourceParams)
      .name(name)
      .datasourceType(DatasourceType.EXTERNALFILE)
      .visibility(Visibility.PUBLICVISIBILITY)
      .externalFileParams(
        (new ExternalFileParams)
          .url(url)
          .fileFormat(FileFormat.CSV)
          .csvFileFormatParams(
            (new CsvFileFormatParams)
              .convert01ToBoolean(false)
              .separatorType(CsvSeparatorType.COMMA)
              .includeHeader(true)))
  }

  private def fromS3(name: String): String = s"https://s3.amazonaws.com/workflowexecutor/examples/data/$name.csv"

  private case class DatasourceForTest(uuid: UUID, datasourceParams: DatasourceParams)

  private object DatasourceForTest {
    def apply(uuid: String, datasourceParams: DatasourceParams): DatasourceForTest = {
      new DatasourceForTest(UUID.fromString(uuid), datasourceParams)
    }
  }
}
