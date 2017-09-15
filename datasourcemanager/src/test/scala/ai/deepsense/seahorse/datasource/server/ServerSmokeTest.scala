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

import scalaj.http._
import org.scalatest.{Matchers, WordSpec}

import ai.deepsense.api.datasourcemanager.ApiClient
import ai.deepsense.api.datasourcemanager.client.DefaultApi
import ai.deepsense.commons.utils.LoggerForCallerClass

class ServerSmokeTest extends WordSpec with Matchers {

  val logger = LoggerForCallerClass()

  "Jetty server" should {
    JettyMain.start(Array.empty)

    "serve datasources" in {
      val response = Http(
        "http://localhost:8080/datasourcemanager/v1/datasources"
      ).header("x-seahorse-userid", UUID.randomUUID().toString).asString
      logger.info(s"Datasources response: ${response.body}")
      response.isNotError shouldBe true
    }

    "respond to client API call" in {
      val apiClient = new ApiClient()
      apiClient.setAdapterBuilder(
        apiClient.getAdapterBuilder().baseUrl("http://localhost:8080/datasourcemanager/v1/"))
      val client = apiClient.createService(classOf[DefaultApi])
      val response = client.getDatasources(UUID.randomUUID().toString).execute()

      withClue(response) {
        response.isSuccessful shouldBe true
      }
    }

    "serve swagger-ui" in {
      val response = Http("http://localhost:8080/datasourcemanager/v1/swagger-ui").asString
      logger.info(s"Swagger ui response: ${response.body}")
      response.isNotError shouldBe true
    }
  }

}
