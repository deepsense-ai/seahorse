/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.server

import scalaj.http._

import org.scalatest.{Matchers, WordSpec}

import io.deepsense.commons.utils.LoggerForCallerClass

class ServerSmokeTest extends WordSpec with Matchers {

  val logger = LoggerForCallerClass()

  "Jetty server" should {
    JettyMain.start(Array.empty)

    "serve datasources" in {
      val response = Http("http://localhost:8080/datasourcemanager/v1/datasources").asString
      logger.info(s"Datasources response: ${response.body}")
      response.isNotError shouldBe true
    }
    "serve swagger-ui" in {
      val response = Http("http://localhost:8080/datasourcemanager/v1/swagger-ui").asString
      logger.info(s"Swagger ui response: ${response.body}")
      response.isNotError shouldBe true
    }
  }

}
