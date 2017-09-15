/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.livy

import io.deepsense.commons.models.Id
import io.deepsense.commons.{StandardSpec, UnitTestSupport}
import io.deepsense.sessionmanager.service.livy.requests.Create

class SessionExecutorRequestBodyBuilderSpec extends StandardSpec with UnitTestSupport {

  "SessionExecutorRequestBodyBuilder" should {
    "generate correct POST requests" in {
      val workflowId = Id.randomId
      val jarPath = "jarPath"
      val depsPath = "hdfs://depsPath/depsFile"
      val depsFile = "depsFile"
      val className = "className"
      val queueHost = "queueHost"
      val queuePort = 1234
      val wmScheme = "http"
      val wmHost = "wmhost"
      val wmPort = "9080"
      val wmAddress = s"$wmScheme://$wmHost:$wmPort"
      val wmUsername = "SomeUsername"
      val wmPassword = "SomePassword"
      val userId = "SomeUserId"

      val builder = new SessionExecutorRequestBodyBuilder(
        className,
        jarPath,
        depsPath,
        queueHost,
        queuePort,
        false,
        wmScheme,
        wmHost,
        wmPort,
        false,
        wmUsername,
        wmPassword
      )

      val request = builder.createSession(workflowId, userId)

      val expected = Create(
        jarPath,
        className,
        Seq(
          "--interactive-mode",
          "-m", queueHost,
          "--message-queue-port", queuePort.toString,
          "--wm-address", wmAddress,
          "--workflow-id", workflowId.toString(),
          "-d", depsFile,
          "--wm-username", wmUsername,
          "--wm-password", wmPassword,
          "--user-id", userId
        ),
        Seq(
          depsPath
        ),
        Map(
          "spark.driver.extraClassPath" -> "__app__.jar",
          "spark.executorEnv.PYTHONPATH" -> depsFile
        )
      )

      request shouldBe expected
    }
  }
}
