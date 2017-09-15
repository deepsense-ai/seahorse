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

import scala.util.{Failure, Success}

import buildinfo.BuildInfo
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import scopt.OptionParser

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.DOperation._
import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameBuilder}
import io.deepsense.deeplang.parameters.ParametersSchema
import io.deepsense.deeplang.{DOperation0To1, ExecutionContext}
import io.deepsense.graph.{Graph, Node}
import io.deepsense.models.workflows._


/**
 * WorkflowExecutor
 * workflow file name has to be passed via command-line parameter
 */
object WorkflowExecutorApp extends Logging {

  private val parser: OptionParser[ExecutionParams]
      = new scopt.OptionParser[ExecutionParams](BuildInfo.name) {
    head(BuildInfo.toString)
    opt[Unit]('r', "generate-report") action {
      (_, c) => c.copy(generateReport = true)
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

  def main(args: Array[String]): Unit = {
    logger.info("Starting WorkflowExecutor...")
    val cmdParams = parser.parse(args, ExecutionParams())

    cmdParams match {
      case None => System.exit(-1)
      case Some(params) =>
        // Read graph
        val workflowWithVariables = loadWorkflow(params.workflowFilename)

        // Run executor
        logger.info("Executing graph!")
        logger.debug("Workflow", workflowWithVariables)
        val executionReport = WorkflowExecutor(workflowWithVariables, params.generateReport)
          .execute()

        executionReport match {
          case Failure(exception) => logger.error("Execution failed:", exception)
          case Success(value) =>
            val result = WorkflowWithResults(
              workflowWithVariables.metadata,
              workflowWithVariables.graph,
              workflowWithVariables.thirdPartyData,
              value
            )

            saveResults(params.outputDirectoryPath, result)

            logger.info(s"result: $result")
        }
    }
  }

  // TODO Reimplement: implement loading workflows from a file
  private def loadWorkflow(filename: String): WorkflowWithVariables = {
    val tmpGraph: Graph = Graph(Set(Node(Node.Id.randomId, Noop())))
    WorkflowWithVariables(
      WorkflowMetadata(WorkflowType.Batch, "1"),
      tmpGraph,
      ThirdPartyData("some data"),
      Variables()
    )
  }

  private def saveResults(outputDir: String, result: WorkflowWithResults): Unit = {
    // TODO Implement
  }

  private case class ExecutionParams(
    workflowFilename: String = "",
    outputDirectoryPath: String = "",
    generateReport: Boolean = false)
}

// FIXME Delete!
case class Noop() extends DOperation0To1[DataFrame] {
  override protected def _execute(context: ExecutionContext)(): DataFrame = {
    val schema = new StructType(Array(StructField("test", StringType)))
    val data: RDD[Row] = context.sparkContext.parallelize(List(Row("testvalue")))
    logger.info("NOP NOP NOP NOP NOP NOP NOP NOP NOP")
    DataFrameBuilder(context.sqlContext).buildDataFrame(schema, data)
  }

  override val parameters: ParametersSchema = ParametersSchema()
  override val name: String = "Noop operation"
  override val id: Id = "271d491c-7018-4133-880d-20d9f4f90051"
}
