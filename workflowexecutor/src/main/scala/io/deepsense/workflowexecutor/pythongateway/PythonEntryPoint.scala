/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.workflowexecutor.pythongateway

import scala.concurrent.Promise

import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.sql.DataFrame
import org.apache.spark.{SparkConf, SparkContext}

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang._

/**
  * An entry point to our application designed to be accessible by Python process.
  */
class PythonEntryPoint(
    val sparkContext: SparkContext,
    val dataFrameStorage: ReadOnlyDataFrameStorage,
    val customOperationDataFrameStorage: CustomOperationDataFrameStorage,
    val operationExecutionDispatcher: OperationExecutionDispatcher)
  extends Logging {

  def getSparkContext: JavaSparkContext = sparkContext

  def getSparkConf: SparkConf = sparkContext.getConf

  def getDataFrame(workflowId: String, dataFrameName: String): DataFrame =
    dataFrameStorage.get(workflowId, dataFrameName).get.sparkDataFrame

  private[pythongateway] val codeExecutorPromise: Promise[PythonCodeExecutor] = Promise()

  private[pythongateway] val pythonPortPromise: Promise[Int] = Promise()

  def registerCodeExecutor(codeExecutor: PythonCodeExecutor): Unit = {
    codeExecutorPromise.success(codeExecutor)
  }

  def reportCallbackServerPort(port: Int): Unit = {
    pythonPortPromise.success(port)
  }

  def retrieveInputDataFrame(
      workflowId: String,
      nodeId: String): DataFrame = {
    customOperationDataFrameStorage.getInputDataFrame(workflowId, nodeId).get
  }

  def registerOutputDataFrame(
      workflowId: String,
      nodeId: String,
      outputDataFrame: DataFrame): Unit = {
    customOperationDataFrameStorage.setOutputDataFrame(workflowId, nodeId, outputDataFrame)
  }

  def executionCompleted(workflowId: String, nodeId: String): Unit =
    operationExecutionDispatcher.executionEnded(workflowId, nodeId, Right())

  def executionFailed(workflowId: String, nodeId: String, error: String): Unit =
    operationExecutionDispatcher.executionEnded(workflowId, nodeId, Left(error))
}
