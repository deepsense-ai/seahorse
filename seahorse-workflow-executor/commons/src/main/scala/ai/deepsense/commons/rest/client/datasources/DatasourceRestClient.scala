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

package ai.deepsense.commons.rest.client.datasources

import java.net.URL
import java.util.UUID

import ai.deepsense.api.datasourcemanager.ApiClient
import ai.deepsense.api.datasourcemanager.client.DefaultApi
import ai.deepsense.api.datasourcemanager.model.Datasource
import ai.deepsense.commons.utils.Logging

class DatasourceRestClient(
    datasourceServerAddress: URL,
    userId: String)
  extends DatasourceClient
  with Logging {

  private val client = {
    val apiClient = new ApiClient()
    apiClient.setAdapterBuilder(
      apiClient.getAdapterBuilder.baseUrl(datasourceServerAddress.toString))
    apiClient.createService(classOf[DefaultApi])
  }

  def getDatasource(uuid: UUID): Option[Datasource] = {
    val response = client.getDatasource(userId, uuid.toString).execute()
    if (response.isSuccessful) {
      Some(response.body)
    } else {
      None
    }
  }
}

class DatasourceRestClientFactory(
    datasourceServerAddress: URL,
    userId: String) extends DatasourceClientFactory {

  override def createClient: DatasourceRestClient = {
    new DatasourceRestClient(datasourceServerAddress, userId)
  }
}

