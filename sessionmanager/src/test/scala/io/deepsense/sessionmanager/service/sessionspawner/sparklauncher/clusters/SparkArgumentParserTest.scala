/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters

import scala.collection.mutable

import org.scalatest.{FunSuite, Matchers}

import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkArgumentParser
import scalaz._
import scalaz.Scalaz._

class SparkArgumentParserTest extends FunSuite with Matchers {

  test("Parsing - positive cases") {
    positiveTestCases.foreach { testCase =>
      SparkArgumentParser.parse(testCase.input) shouldEqual testCase.output.success
    }
  }

  test("Parsing - multiple conf options") {
    val mulitpleConf =
      """
        |--conf "spark.executor.extraJavaOptions=-XX:+PrintGCDetails -XX:+PrintGCTimeStamps"
        | --conf "spark.driver.extraJavaOptions=-XX:+PrintGCDetails"
        |""".stripMargin


    val multipleConfValues = mutable.Set[String](
      "spark.executor.extraJavaOptions=-XX:+PrintGCDetails -XX:+PrintGCTimeStamps",
      "spark.driver.extraJavaOptions=-XX:+PrintGCDetails")
    val mutipleConfOutput = Map("--conf" -> multipleConfValues)
    SparkArgumentParser.parse(mulitpleConf) shouldEqual mutipleConfOutput.success
  }

  test("Parsing - negative cases") {
    invalidInputs.foreach { input =>
      SparkArgumentParser.parse(input).isFailure shouldBe true
    }
  }

  val positiveTestCases = Seq(
    TestCase("--executor-memory 20G", Map("--executor-memory" -> Set("20G"))),
    TestCase("--verbose", Map("--verbose" -> null)), // non value parameter
    TestCase("--verbose --executor-memory 20G", Map(
      "--verbose" -> null,
      "--executor-memory" -> Set("20G")
    )),
    TestCase("--executor-memory 10 --num-executors 10",
      Map(
        "--executor-memory" -> Set("10"),
        "--num-executors" -> Set("10")
      )
    ),
    TestCase("  --executor-memory  20G   --num-executors   20",
      Map(
        "--executor-memory" -> Set("20G"),
        "--num-executors" -> Set("20")
      )
    ),
    TestCase(
      """--executor-memory 30G
        |  --num-executors 30""".stripMargin,
      Map(
        "--executor-memory" -> Set("30G"),
        "--num-executors" -> Set("30")
      )
    ),
    TestCase(
      """--conf "spark.executor.extraJavaOptions=-XX:+PrintGCDetails -XX:+PrintGCTimeStamps" """,
      Map(
        "--conf" ->
          Set("spark.executor.extraJavaOptions=-XX:+PrintGCDetails -XX:+PrintGCTimeStamps")
      )
    )
  )

  case class TestCase(input: String, output: Map[String, Set[String]])

  val invalidInputs = Seq(
    "--non-existing-key-in-spark 50",
    "-executor-memory 50",
    "-- executor-memory 50",
    "--executor-memory 50 --",
    "--executor-memory 50 50"
  )

}
