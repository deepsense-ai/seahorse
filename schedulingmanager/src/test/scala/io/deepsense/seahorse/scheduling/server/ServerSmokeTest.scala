/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.server

import scalaj.http._

import org.scalatest.{Matchers, WordSpec}

import io.deepsense.commons.utils.LoggerForCallerClass
import io.deepsense.seahorse.scheduling.server.JettyMain

class ServerSmokeTest extends WordSpec with Matchers {

  val logger = LoggerForCallerClass()

  "Jetty server" should {
    JettyMain.start(Array.empty)

    "serve schedules" in {
      val response = Http("http://localhost:8080/schedulingmanager/v1/workflow-schedules").asString
      logger.info(s"Scheduling manager response: ${response.body}")
      response.isNotError shouldBe true
    }
    "serve swagger-ui" in {
      val response = Http("http://localhost:8080/schedulingmanager/v1/swagger-ui").asString
      logger.info(s"Swagger ui response: ${response.body}")
      response.isNotError shouldBe true
    }
  }

}
