/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.workflowmanager.versionconverter

import java.net.URL
import java.util.UUID

import scala.util.{Try, Success, Failure}

import ai.deepsense.api.datasourcemanager.ApiClient
import ai.deepsense.api.datasourcemanager.client.DefaultApi
import ai.deepsense.api.datasourcemanager.model.DatasourceParams
import ai.deepsense.commons.utils.Logging

class DatasourcesClient(datasourceServerAddress: URL, userId: UUID, userName: String) extends Logging {

  private val client = {
    val apiClient = new ApiClient()
    apiClient.setAdapterBuilder(
      apiClient.getAdapterBuilder.baseUrl(datasourceServerAddress.toString))
    apiClient.createService(classOf[DefaultApi])
  }

  def insertDatasource(uuid: UUID, datasourceParams: DatasourceParams): Try[Unit] = {
    val response = client.putDatasource(userId.toString, userName, uuid.toString, datasourceParams).execute()
    logger.info(s"Adding datasource, userId = $userId, userName = $userName," +
      s"uuid = $uuid, params = $datasourceParams")
    if (response.isSuccessful) { Success {
      logger.info(s"Successfully added datasource; body = ${response.body()}")
    }} else {
      Failure(new RuntimeException(
        s"There was a problem with adding datasource," +
          s"code: ${response.code()}, error body: ${response.errorBody().string()}."
      ))
    }
  }
}
