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

package io.deepsense.deeplang

import java.util.concurrent.TimeUnit

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame => SparkDataFrame, SQLContext}

import io.deepsense.commons.models.Id
import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.doperables.ReportLevel.ReportLevel
import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameBuilder}
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.entitystorage.{EntityStorageClient, UniqueFilenameUtil}

case class CommonExecutionContext(
    sparkContext: SparkContext,
    sqlContext: SQLContext,
    inferContext: InferContext,
    fsClient: FileSystemClient,
    reportLevel: ReportLevel,
    tenantId: String,
    dataFrameStorage: DataFrameStorage,
    pythonCodeExecutor: Future[PythonCodeExecutor],
    customOperationExecutor: CustomOperationExecutor) extends Logging {

  def createExecutionContext(workflowId: Id, nodeId: Id): ExecutionContext = {
    logger.debug("Waiting for python code executor")
    val executor: PythonCodeExecutor =
      Await.result(pythonCodeExecutor, Duration(5, TimeUnit.SECONDS))

    ExecutionContext(
      sparkContext,
      sqlContext,
      inferContext,
      fsClient,
      reportLevel,
      tenantId,
      ContextualDataFrameStorage(dataFrameStorage, workflowId, nodeId),
      ContextualPythonCodeExecutor(executor, customOperationExecutor, workflowId, nodeId))
  }
}

/** Holds information needed by DOperations and DMethods during execution. */
case class ExecutionContext(
    sparkContext: SparkContext,
    sqlContext: SQLContext,
    inferContext: InferContext,
    fsClient: FileSystemClient,
    reportLevel: ReportLevel,
    tenantId: String,
    dataFrameStorage: ContextualDataFrameStorage,
    pythonCodeExecutor: ContextualPythonCodeExecutor) extends Logging {

  def dataFrameBuilder: DataFrameBuilder = inferContext.dataFrameBuilder

  def entityStorageClient: EntityStorageClient = inferContext.entityStorageClient

  def uniqueFsFileName(entityCategory: String): String =
    UniqueFilenameUtil.getUniqueFsFilename(tenantId, entityCategory)
}

case class ContextualDataFrameStorage(
    dataFrameStorage: DataFrameStorage,
    workflowId: Id,
    nodeId: Id) {

  def store(dataFrame: DataFrame): Unit =
    dataFrameStorage.put(workflowId, nodeId.toString, dataFrame)

  def setInputDataFrame(dataFrame: SparkDataFrame): Unit =
    dataFrameStorage.setInputDataFrame(workflowId, nodeId, dataFrame)

  def getOutputDataFrame(): Option[SparkDataFrame] =
    dataFrameStorage.getOutputDataFrame(workflowId, nodeId)
}

case class ContextualPythonCodeExecutor(
    pythonCodeExecutor: PythonCodeExecutor,
    customOperationExecutor: CustomOperationExecutor,
    workflowId: Id,
    nodeId: Id) extends Logging {

  def validate(code: String): Boolean = pythonCodeExecutor.validate(code)

  def run(code: String): Unit = {
    val execution = customOperationExecutor.execute(workflowId, nodeId)
    pythonCodeExecutor.run(workflowId.toString, nodeId.toString, code)
    logger.debug("Waiting for python operation to finish")
    Await.result(execution, Duration.Inf)
  }
}
