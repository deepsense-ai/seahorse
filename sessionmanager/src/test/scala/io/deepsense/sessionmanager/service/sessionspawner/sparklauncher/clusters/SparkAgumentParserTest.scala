/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters

import org.scalatest.{Matchers, FunSuite}

import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkAgumentParser

import scalaz._
import scalaz.Scalaz._

class SparkAgumentParserTest extends FunSuite with Matchers {

  test("Parsing - positive cases") {
    positiveTestCases.foreach { testCase =>
      SparkAgumentParser.parse(testCase.input) shouldEqual testCase.output.success
    }
  }
  test("Parsing - negative cases") {
    invalidInputs.foreach { input =>
      SparkAgumentParser.parse(input).isFailure shouldBe true
    }
  }

  val positiveTestCases = Seq(
    TestCase("--executor-memory 20G", Map("--executor-memory" -> "20G")),
    TestCase("--verbose", Map("--verbose" -> null)), // non value parameter
    TestCase("--verbose --executor-memory 20G", Map(
      "--verbose" -> null,
      "--executor-memory" -> "20G"
    )),
    TestCase("--executor-memory 20G --num-executors 50",
      Map(
        "--executor-memory" -> "20G",
        "--num-executors" -> "50"
      )
    ),
    TestCase("--executor-memory 20G   --num-executors 50",
      Map(
        "--executor-memory" -> "20G",
        "--num-executors" -> "50"
      )
    ),
    TestCase("--executor-memory 20G \n  --num-executors 50",
      Map(
        "--executor-memory" -> "20G",
        "--num-executors" -> "50"
      )
    )
  )

  case class TestCase(input: String, output: Map[String, String])

  val invalidInputs = Seq(
    "--non-existing-key-in-spark 50",
    "-executor-memory 50",
    "-- executor-memory 50",
    "--executor-memory 50 --",
    "--executor-memory 50 50"
  )

}
