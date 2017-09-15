/**
 * Copyright 2015, CodiLime Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.workflowexecutor

import scala.math.random

import buildinfo.BuildInfo
import org.apache.spark._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{SaveMode, Row, SQLContext}
import org.apache.spark.sql.types._


case class Config(
  workflowFilename: String = null,
  outputDirectoryPath: String = null,
  executionReport: Boolean = false)

// scalastyle:off println
/**
 * WorkflowExecutor
 * workflow file name has to be passed via command-line parameter
 * TODO: replace TODOs with proper logging
 */
object WorkflowExecutor {

  var workflowFilename: String = _
  var outputDirectoryPath: String = _
  var executionReport: Boolean = _

  def main(args: Array[String]): Unit = {
    println("Starting WorkflowExecutor")
    val parser = new scopt.OptionParser[Config](BuildInfo.name) {
      head(BuildInfo.toString)
      opt[Unit]('r', "execution-report") action {
        (_, c) => c.copy(executionReport = true)
      } text "compute ExecutionReport (very time consuming option)"
      opt[String]('w', "workflow-filename") required() valueName "<file-name>" action {
        (x, c) => c.copy(workflowFilename = x)
      } text "workflow filename"
      opt[String]('o', "output-directory") required() valueName "<dir-path>" action {
        (x, c) => c.copy(outputDirectoryPath = x)
      } text
        "output directory path; directory will be created if it does not exists; " +
        "execution fails if any file is to be overwritten"
      help("help") text "print this help message"
      version("version") text "print product version and exit"
      note("See http://deepsense.io for more details")
    }
    parser.parse(args, Config()) map {
      config =>
        println("Parameter execution-report: " + config.executionReport)
        println("Parameter output-directory: " + config.outputDirectoryPath)
        println("Parameter workflow-filename: " + config.workflowFilename)
        executionReport = config.executionReport
        outputDirectoryPath = config.outputDirectoryPath
        workflowFilename = config.workflowFilename
    } getOrElse {
      // Command-line arguments are bad, usage message will have been displayed
      System.exit(-1)
    }



    // PoC: Print file content
    val lines = scala.io.Source.fromFile(workflowFilename).mkString
    println("FILE " + workflowFilename + " CONTENT: " + lines)



    // PoC: Execute some Spark operations
    val conf = new SparkConf().setAppName("Spark Pi")
    val sc = new SparkContext(conf)
    val slices = 2
    val n = math.min(1000L * slices, Int.MaxValue).toInt // avoid overflow
    val count = sc.parallelize(1 until n, slices).map { i =>
        val x = random * 2 - 1
        val y = random * 2 - 1
        if (x*x + y*y < 1) 1 else 0
      }.reduce(_ + _)
    println("DeepSense.io computed PI constant is roughly " + 4.0 * count / n)



    // PoC: Writing something to output directory
    // tested:
    //   -o hdfs:///a/b/c/   (write on HDFS tied with cluster)
    //   -o file:///a/b/c/
    //      on mode cluster: write on ApplicationMaster machine / Spark standalone cluster master,
    //      on mode client: write on client machine (submitter's machine)
    val sqlContext = new SQLContext(sc)
    val rdd: RDD[Row] = sc.parallelize((1 until n).map( x => Row.fromTuple( (x, "i co?") )))
    val schema = StructType(Seq(StructField("num", IntegerType), StructField("text", StringType)))
    val df = sqlContext.createDataFrame(rdd, schema)
    // NOTE: Writing via DataFrameWriter automatically creates parent directory (and its parents...)
    df.write.format("parquet").mode(SaveMode.ErrorIfExists).save(outputDirectoryPath + "/something")
    // df.save(outputDirectoryPath)

    sc.stop()
    println("Ending WorkflowExecutor")
  }
}
// scalastyle:on println
