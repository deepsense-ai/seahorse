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

package ai.deepsense.workflowexecutor.session.storage

import scala.collection.concurrent.TrieMap

import org.apache.spark.sql.{DataFrame => SparkDataFrame}

import ai.deepsense.commons.models.Id
import ai.deepsense.deeplang.DataFrameStorage

class DataFrameStorageImpl extends DataFrameStorage {

  private val inputDataFrames: TrieMap[(Id, Id, Int), SparkDataFrame] = TrieMap.empty
  private val outputDataFrames: TrieMap[(Id, Id, Int), SparkDataFrame] = TrieMap.empty

  override def getInputDataFrame(
      workflowId: Id, nodeId: Id, portNumber: Int): Option[SparkDataFrame] =
    inputDataFrames.get((workflowId, nodeId, portNumber))

  override def setInputDataFrame(
      workflowId: Id, nodeId: Id, portNumber: Int, dataFrame: SparkDataFrame): Unit =
    inputDataFrames.put((workflowId, nodeId, portNumber), dataFrame)

  override def removeNodeInputDataFrames(workflowId: Id, nodeId: Id, portNumber: Int) : Unit =
    inputDataFrames.remove((workflowId, nodeId, portNumber))

  override def removeNodeInputDataFrames(workflowId: Id, nodeId: Id) : Unit =
    inputDataFrames.retain((k, _) => k._1 != workflowId || k._2 != nodeId)

  override def getOutputDataFrame(
      workflowId: Id, nodeId: Id, portNumber: Int): Option[SparkDataFrame] =
    outputDataFrames.get((workflowId, nodeId, portNumber))

  override def setOutputDataFrame(
      workflowId: Id, nodeId: Id, portNumber: Int, dataFrame: SparkDataFrame): Unit =
    outputDataFrames.put((workflowId, nodeId, portNumber), dataFrame)

  override def removeNodeOutputDataFrames(workflowId: Id, nodeId: Id) : Unit =
    outputDataFrames.retain((k, _) => k._1 != workflowId || k._2 != nodeId)
}
