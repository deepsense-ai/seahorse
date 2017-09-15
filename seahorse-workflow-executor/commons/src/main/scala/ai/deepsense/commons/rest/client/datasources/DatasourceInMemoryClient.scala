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
import ai.deepsense.commons.rest.client.datasources.DatasourceTypes.{DatasourceId, DatasourceList, DatasourceMap}
import ai.deepsense.commons.utils.CollectionExtensions

class DatasourceInMemoryClient(datasourceList: DatasourceList) extends DatasourceClient {
  import CollectionExtensions._

  def getDatasource(uuid: UUID): Option[Datasource] = {
    datasourceMap.get(uuid.toString)
  }

  private val datasourceMap = datasourceList.lookupBy(_.getId)
}

class DatasourceInMemoryClientFactory(datasourceMap: DatasourceList)
  extends DatasourceClientFactory {

  override def createClient: DatasourceClient = {
    new DatasourceInMemoryClient(datasourceMap)
  }
}
