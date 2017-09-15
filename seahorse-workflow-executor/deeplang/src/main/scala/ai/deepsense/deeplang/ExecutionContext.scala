/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame => SparkDataFrame}

import ai.deepsense.commons.mail.EmailSender
import ai.deepsense.commons.models.Id
import ai.deepsense.commons.rest.client.{NotebookRestClient, NotebooksClientFactory}
import ai.deepsense.commons.utils.Logging
import ai.deepsense.deeplang.OperationExecutionDispatcher.Result
import ai.deepsense.deeplang.doperables.dataframe.DataFrameBuilder
import ai.deepsense.deeplang.inference.InferContext
import ai.deepsense.sparkutils.SparkSQLSession

case class CommonExecutionContext(
    sparkContext: SparkContext,
    sparkSQLSession: SparkSQLSession,
    inferContext: InferContext,
    executionMode: ExecutionMode,
    fsClient: FileSystemClient,
    tempPath: String,
    libraryPath: String,
    innerWorkflowExecutor: InnerWorkflowExecutor,
    dataFrameStorage: DataFrameStorage,
    notebooksClientFactory: Option[NotebooksClientFactory],
    emailSender: Option[EmailSender],
    customCodeExecutionProvider: CustomCodeExecutionProvider) extends Logging {

  def createExecutionContext(workflowId: Id, nodeId: Id): ExecutionContext =
    ExecutionContext(
      sparkContext,
      sparkSQLSession,
      inferContext,
      executionMode,
      fsClient,
      tempPath,
      libraryPath,
      innerWorkflowExecutor,
      ContextualDataFrameStorage(dataFrameStorage, workflowId, nodeId),
      notebooksClientFactory.map(_.createNotebookForNode(workflowId, nodeId)),
      emailSender,
      ContextualCustomCodeExecutor(customCodeExecutionProvider, workflowId, nodeId))
}

object CommonExecutionContext {

  def apply(context: ExecutionContext): CommonExecutionContext =
    CommonExecutionContext(
      context.sparkContext,
      context.sparkSQLSession,
      context.inferContext,
      context.executionMode,
      context.fsClient,
      context.tempPath,
      context.libraryPath,
      context.innerWorkflowExecutor,
      context.dataFrameStorage.dataFrameStorage,
      context.notebooksClient.map(_.toFactory),
      context.emailSender,
      context.customCodeExecutor.customCodeExecutionProvider)
}

/** Holds information needed by DOperations and DMethods during execution. */
case class ExecutionContext(
    sparkContext: SparkContext,
    sparkSQLSession: SparkSQLSession,
    inferContext: InferContext,
    executionMode: ExecutionMode,
    fsClient: FileSystemClient,
    tempPath: String,
    libraryPath: String,
    innerWorkflowExecutor: InnerWorkflowExecutor,
    dataFrameStorage: ContextualDataFrameStorage,
    notebooksClient: Option[NotebookRestClient],
    emailSender: Option[EmailSender],
    customCodeExecutor: ContextualCustomCodeExecutor) extends Logging {

  def dataFrameBuilder: DataFrameBuilder = inferContext.dataFrameBuilder
}

case class ContextualDataFrameStorage(
    dataFrameStorage: DataFrameStorage,
    workflowId: Id,
    nodeId: Id) {

  def setInputDataFrame(portNumber: Int, dataFrame: SparkDataFrame): Unit =
    dataFrameStorage.setInputDataFrame(workflowId, nodeId, portNumber, dataFrame)

  def removeNodeInputDataFrames(portNumber: Int): Unit =
    dataFrameStorage.removeNodeInputDataFrames(workflowId, nodeId, portNumber)

  def removeNodeInputDataFrames() : Unit =
    dataFrameStorage.removeNodeInputDataFrames(workflowId, nodeId)

  def getOutputDataFrame(portNumber: Int): Option[SparkDataFrame] =
    dataFrameStorage.getOutputDataFrame(workflowId, nodeId, portNumber)

  def setOutputDataFrame(portNumber: Int, dataFrame: SparkDataFrame): Unit =
    dataFrameStorage.setOutputDataFrame(workflowId, nodeId, portNumber, dataFrame)

  def removeNodeOutputDataFrames(): Unit =
    dataFrameStorage.removeNodeOutputDataFrames(workflowId, nodeId)

  def withInputDataFrame[T](portNumber: Int, dataFrame: SparkDataFrame)(block: => T) : T = {
    setInputDataFrame(portNumber, dataFrame)
    try {
      block
    } finally {
      removeNodeInputDataFrames(portNumber)
    }
  }
}

case class ContextualCustomCodeExecutor(
    customCodeExecutionProvider: CustomCodeExecutionProvider,
    workflowId: Id,
    nodeId: Id) extends Logging {

  def isPythonValid: (String) => Boolean = customCodeExecutionProvider.pythonCodeExecutor.isValid
  def isRValid: (String) => Boolean = customCodeExecutionProvider.rCodeExecutor.isValid

  def runPython: (String) => Result = run(_, customCodeExecutionProvider.pythonCodeExecutor)
  def runR: (String) => Result = run(_, customCodeExecutionProvider.rCodeExecutor)

  private def run(code: String, executor: CustomCodeExecutor): Result = {
    val result =
      customCodeExecutionProvider.operationExecutionDispatcher.executionStarted(workflowId, nodeId)
    executor.run(workflowId.toString, nodeId.toString, code)
    logger.debug("Waiting for user's custom operation to finish")
    Await.result(result, Duration.Inf)
  }
}
