package io.deepsense.e2etests

import java.net.URL
import java.util.UUID

import io.deepsense.api.datasourcemanager.ApiClient
import io.deepsense.api.datasourcemanager.client.DefaultApi
import io.deepsense.api.datasourcemanager.model.DatasourceParams
import io.deepsense.commons.utils.Logging

class DatasourcesClient(datasourceServerAddress: URL, userId: UUID, userName: String) extends Logging {

  private val client = {
    val apiClient = new ApiClient()
    apiClient.setAdapterBuilder(
      apiClient.getAdapterBuilder.baseUrl(datasourceServerAddress.toString))
    apiClient.createService(classOf[DefaultApi])
  }

  def insertDatasource(uuid: UUID, datasourceParams: DatasourceParams): Unit = {
    val response = client.putDatasource(userId.toString, userName, uuid.toString, datasourceParams).execute()
    logger.info(s"Adding datasource, userId = $userId, userName = $userName," +
      s"uuid = $uuid, params = $datasourceParams")
    if (response.isSuccessful) {
      logger.info(s"Successfully added datasource; body = ${response.body()}")
    } else {
      throw new Exception(
        s"There was a problem with adding datasource," +
          s"code: ${response.code()}, error body: ${response.errorBody().string()}."
      )
    }
  }
}
