/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.server

import java.util.UUID

import scalaj.http._
import org.scalatest.{Matchers, WordSpec}

import io.deepsense.api.datasourcemanager.ApiClient
import io.deepsense.api.datasourcemanager.client.DefaultApi
import io.deepsense.commons.utils.LoggerForCallerClass

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
