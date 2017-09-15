/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSuite, Matchers}

import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkArgumentParser._

class SeahorseSparkLauncherSpec extends FunSuite with Matchers  with MockitoSugar {
  test("Merging Configuration Option") {
    testCases.foreach { testCase =>
      val output = testCase.inputArgs.updateConfOptions(testCase.inputKey, testCase.inputValue)
      output shouldBe testCase.output
    }
  }

  val testCases = Seq(
    TestCase("key", "value", Map("--conf" -> Set("otherKey=5"), "--con" -> Set("value=otherValue")),
      Map("--conf" -> Set("otherKey=5", "key=value"), "--con" -> Set("value=otherValue"))),
    TestCase("key", "value", Map(("--conf" -> Set("key=valueOther", "key2=value2"))),
      Map("--conf" -> Set("key=valueOther,value", "key2=value2")))
  )

  case class TestCase(
    inputKey: String,
    inputValue: String,
    inputArgs: Map[String, Set[String]],
    output: Map[String, Set[String]])

}
