/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters

import org.apache.spark.launcher.SparkLauncher
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.scalatest.{FunSuite, Matchers}

import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters.SeahorseSparkLauncher.RichSparkLauncher


class SeahorseSparkLauncherSpec extends FunSuite with Matchers  with MockitoSugar {
  test("Merging Configuration Option - no merging needed") {
    val sparkMock = mock[SparkLauncher]
    val richSparkLauncher = new RichSparkLauncher(sparkMock)
    richSparkLauncher.mergeConfOption("key", "value",
      Map[String, String]("--conf" -> "otherKey=5", "--con" -> "value=otherValue"))

    verify(sparkMock, times(1)).setConf("key", "value")
  }
  test("Merging Configuration Option - merging needed") {
    val sparkMock = mock[SparkLauncher]
    val richSparkLauncher = new RichSparkLauncher(sparkMock)
    richSparkLauncher.mergeConfOption("key", "value",
      Map[String, String]("--conf" -> "key=valueOther"))

    verify(sparkMock, times(1)).setConf("key", "valueOther,value")
  }

}
