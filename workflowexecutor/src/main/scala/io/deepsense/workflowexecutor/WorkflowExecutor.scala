/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowexecutor

// scalastyle:off println
import scala.math.random

import org.apache.spark._

/** Computes an approximation to pi */
object WorkflowExecutor {

  val ExperimentJsonFilename = "workflow.json"

  def main(args: Array[String]): Unit = {
    import sys.process._
    "ls -al" !

    val lines = scala.io.Source.fromFile(ExperimentJsonFilename).mkString
    println("FILE " + ExperimentJsonFilename + " CONTENT: " + lines)

    val conf = new SparkConf().setAppName("Spark Pi")
    val spark = new SparkContext(conf)
    val slices = if (args.length > 0) args(0).toInt else 2
    println("Slices count (2 if not given as commandline parameter): " + slices)
    val n = math.min(100000L * slices, Int.MaxValue).toInt // avoid overflow
    val count = spark.parallelize(1 until n, slices).map { i =>
        val x = random * 2 - 1
        val y = random * 2 - 1
        if (x*x + y*y < 1) 1 else 0
      }.reduce(_ + _)
    println("DeepSense.io computed PI constant is roughly " + 4.0 * count / n)
    spark.stop()
  }
}
// scalastyle:on println
