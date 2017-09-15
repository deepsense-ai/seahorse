/**
  * Copyright (c) 2016, CodiLime Inc.
  */

package io.deepsense.sessionmanager

import scalaz.Failure

import com.google.inject.Guice
import org.scalatest.Inside
import org.scalatest.concurrent.Futures
import org.scalatest.time.{Second, Seconds, Span}

import io.deepsense.commons.StandardSpec
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters.SeahorseSparkLauncher
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkArgumentParser.UnknownOption
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.{SparkLauncherConfig, SparkLauncherError}

class SparkLauncherErrorHandlingTest extends StandardSpec with Futures with Inside {

  import io.deepsense.sessionmanager.service.TestData._

  private implicit val patience = PatienceConfig(Span(10, Seconds), Span(1, Second))
  private val sparkLauncherConfig = {
    val injector = Guice.createInjector(new SessionManagerAppModule())
    injector.getInstance(classOf[SparkLauncherConfig])
  }

  "Unknown opt handling (pre spark launcher)" in {
    val clusterDetails = someClusterDetails.copy (
      params = Some("--non-existing-parameter some-value")
    )
    val creating = SeahorseSparkLauncher(someSessionConfig(), sparkLauncherConfig, clusterDetails)
    inside(creating) { case Failure(error) =>
      error shouldBe an [UnknownOption]
    }
  }

  "Unknown illegal conf key in params" in {
    val clusterDetails = someClusterDetails.copy (
      params = Some("--conf not-spark.executor.extraJavaOptions=-XX:+PrintGCDetails")
    )
    val creating = SeahorseSparkLauncher(someSessionConfig(), sparkLauncherConfig, clusterDetails)
    inside(creating) { case Failure(error) =>
      error shouldBe an [SparkLauncherError]
      error.getMessage should include ("'key' must start with 'spark.'")
    }

  }

}
