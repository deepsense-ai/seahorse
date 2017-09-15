/**
  * Copyright (c) 2016, CodiLime Inc.
  */

package io.deepsense.sessionmanager

import com.google.inject.Guice
import org.scalatest.concurrent.Futures
import org.scalatest.time.{Second, Seconds, Span}

import io.deepsense.commons.StandardSpec
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.SparkLauncherSessionSpawner
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkAgumentParser.UnknownOption

class SparkLauncherErrorHandlingTest extends StandardSpec with Futures {

  import io.deepsense.sessionmanager.service.TestData._

  private implicit val patience = PatienceConfig(Span(10, Seconds), Span(1, Second))
  private val sessionSpawner = {
    val injector = Guice.createInjector(new SessionManagerAppModule())
    injector.getInstance(classOf[SparkLauncherSessionSpawner])
  }

  "Unknown opt handling (pre spark launcher)" in {
    val clusterDetails = someClusterDetails.copy (
      params = Some("--non-existing-parameter some-value")
    )
    val creating = sessionSpawner.createSession(someSessionConfig(), clusterDetails)
    creating.failed.futureValue shouldBe an [UnknownOption]
  }

}
