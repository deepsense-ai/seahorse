/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.server

import org.scalatest.{Matchers, WordSpec}
import scalaj.http._

import io.deepsense.commons.utils.LoggerForCallerClass

class ServerSmokeTest extends WordSpec with Matchers {

  val logger = LoggerForCallerClass()

  "Jetty server" should {
    "serve datasources" in {
      JettyMain.start(Array.empty)
      val response = Http("http://localhost:8080/datasources").asString
      logger.info(s"Response: ${response.body}")
      response.code shouldEqual 200
    }
  }

}
