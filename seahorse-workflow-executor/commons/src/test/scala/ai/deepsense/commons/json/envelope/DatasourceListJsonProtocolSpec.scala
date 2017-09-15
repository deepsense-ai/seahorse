/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.commons.json.envelope

import org.joda.time.DateTime
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import ai.deepsense.api.datasourcemanager.model.{AccessLevel, Datasource, DatasourceParams, DatasourceType}
import ai.deepsense.commons.datasource.DatasourceTestData
import ai.deepsense.commons.json.datasources.DatasourceListJsonProtocol

class DatasourceListJsonProtocolSpec
  extends WordSpec
  with MockitoSugar
  with Matchers {

  val uuid = "123e4567-e89b-12d3-a456-426655440000"
  val externalFile = DatasourceType.EXTERNALFILE

  val dsList = List(DatasourceTestData.multicharSeparatorLibraryCsvDatasource)

  "DatasourceJsonProtocolSpec" should {
    "serialize and deserialize single datasource" in {
      val datasourcesJson = DatasourceListJsonProtocol.toString(dsList)
      val asString = datasourcesJson.toString
      val datasources = DatasourceListJsonProtocol.fromString(asString)
      info(s"Datasource: $datasources, json: $asString")
      datasources should contain theSameElementsAs dsList
    }

    "serialize no datasource" in {
      val datasourcesJson = DatasourceListJsonProtocol.toString(List.empty[Datasource])
      datasourcesJson shouldBe "[]"
    }
  }
}
