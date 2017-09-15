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

package ai.deepsense.commons.rest.client.datasources

import java.util.UUID

import ai.deepsense.api.datasourcemanager.model.Datasource
import ai.deepsense.commons.serialization.Serialization
import ai.deepsense.commons.{StandardSpec, UnitTestSupport}

class DatasourceInMemoryClientSpec extends StandardSpec
  with UnitTestSupport
  with Serialization {
  val uuid = "123e4567-e89b-12d3-a456-426655440000"
  val notPresentUuid = "123e4567-e89b-12d3-a456-426655440001"
  val ds = getTestDatasource
  val testDatasourceList = List(ds)

  "DatasourceInMemoryClient" should {
    val datasourceClient = new DatasourceInMemoryClient(testDatasourceList)
    "return datasource if present" in {
      val dsOpt = datasourceClient.getDatasource(UUID.fromString(uuid))
      dsOpt shouldBe Some(ds)
    }
    "return None if datasource was not present in list" in {
      val dsOpt = datasourceClient.getDatasource(UUID.fromString(notPresentUuid))
      dsOpt shouldBe None
    }
  }

  private def getTestDatasource: Datasource = {
    val ds = new Datasource
    ds.setId(uuid)
    ds
  }
}
