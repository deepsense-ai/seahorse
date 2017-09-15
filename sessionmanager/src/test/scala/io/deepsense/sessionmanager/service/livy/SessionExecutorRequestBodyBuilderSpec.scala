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
        false
      )

      val request = builder.createSession(workflowId)

      val expected = Create(
        jarPath,
        className,
        Seq(
          "--interactive-mode",
          "-m", queueHost,
          "--message-queue-port", queuePort.toString,
          "--wm-address", wmAddress,
          "-j", workflowId.toString(),
          "-d", depsFile
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
