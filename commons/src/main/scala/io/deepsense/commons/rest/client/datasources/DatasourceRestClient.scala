/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.commons.rest.client.datasources

import java.net.URL
import java.util.UUID

import io.deepsense.api.datasourcemanager.ApiClient
import io.deepsense.api.datasourcemanager.client.DefaultApi
import io.deepsense.api.datasourcemanager.model.{Datasource, DatasourceParams}
import io.deepsense.commons.utils.Logging

class DatasourceRestClient(
    datasourceServerAddress: URL,
    userId: String)
  extends DatasourceClient
  with Logging {

  val apiClient = new ApiClient()
  apiClient.setAdapterBuilder(
    apiClient.getAdapterBuilder().baseUrl(datasourceServerAddress.toString))

  def getDatasource(uuid: UUID): Option[Datasource] = {
    val client = apiClient.createService(classOf[DefaultApi])  // TODO can this be extracted?
    val response = client.getDatasource(userId, uuid.toString).execute()
    if (response.isSuccessful()) {
      Some(response.body)
    } else {
      None
    }
  }

  def addDatasource(userName: String, datasourceParams: DatasourceParams): Unit = {
    val client = apiClient.createService(classOf[DefaultApi])
    val newUUID = UUID.randomUUID().toString
    val response = client.putDatasource(userId, userName, newUUID, datasourceParams).execute()
    if (response.isSuccessful()) {
      logger.info("Added datasource; {}", response.body())
    } else {
      logger.error("There was a problem with adding datasource; {}", response.body())
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

