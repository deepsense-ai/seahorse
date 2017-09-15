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
      val className = "className"
      val queueHost = "queueHost"
      val queuePort = 1234
      val pyExecutorDir = "we"
      val pyExecutorJar = "we2.jar"
      val pySparkDir = "spark"
      val pySparkZip = "pyspark.zip"
      val wmScheme = "http"
      val wmHost = "wmhost"
      val wmPort = "9080"
      val wmAddress = s"$wmScheme://$wmHost:$wmPort"
      val kmDir = "kmdir"
      val kmZip = "km.zip"
      val pikaZip = "pika.zip"

      val builder = new SessionExecutorRequestBodyBuilder(
        className,
        jarPath,
        queueHost,
        queuePort,
        false,
        pyExecutorDir,
        pyExecutorJar,
        pySparkDir,
        pySparkZip,
        wmScheme,
        wmHost,
        wmPort,
        false,
        kmDir,
        kmZip,
        pikaZip
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
          "-p", pyExecutorJar,
          "-z", pySparkZip,
          "-j", workflowId.toString(),
          "--kernel-manager-archive", kmZip
        ),
        Seq(
          s"$pyExecutorDir/$pyExecutorJar",
          s"$pySparkDir/$pySparkZip",
          s"$kmDir/$kmZip",
          pikaZip
        ),
        Map(
          "spark.driver.extraClassPath" -> pyExecutorJar
        )
      )

      request shouldBe expected
    }
  }
}
