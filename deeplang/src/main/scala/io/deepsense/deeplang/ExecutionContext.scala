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

import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

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
    dataFrameStorage: DataFrameStorage) {

  def createExecutionContext(workflowId: Id, nodeId: Id): ExecutionContext = {
    ExecutionContext(
      sparkContext,
      sqlContext,
      inferContext,
      fsClient,
      reportLevel,
      tenantId,
      ContextualDataFrameStorage(dataFrameStorage, workflowId, nodeId))
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
    dataFrameStorage: ContextualDataFrameStorage) extends Logging {

  def dataFrameBuilder: DataFrameBuilder = inferContext.dataFrameBuilder

  def entityStorageClient: EntityStorageClient = inferContext.entityStorageClient

  def uniqueFsFileName(entityCategory: String): String =
    UniqueFilenameUtil.getUniqueFsFilename(tenantId, entityCategory)
}

case class ContextualDataFrameStorage(
  dataFrameStorage: DataFrameStorage,
  workflowId: Id,
  nodeId: Id) {

  def store(dataFrame: DataFrame): Unit = {
    dataFrameStorage.put(workflowId, nodeId.toString, dataFrame)
  }
}
