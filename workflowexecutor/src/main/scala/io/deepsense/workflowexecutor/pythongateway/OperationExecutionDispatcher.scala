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

import scala.collection.concurrent.TrieMap
import scala.concurrent.{Future, Promise}

import io.deepsense.commons.models.Id
import io.deepsense.deeplang.CustomOperationExecutor
import io.deepsense.graph.Node
import io.deepsense.models.workflows.Workflow

class OperationExecutionDispatcher {

  import OperationExecutionDispatcher._

  private val operationEndPromises: TrieMap[OperationId, Promise[Unit]] = TrieMap.empty

  def executionStarted(workflowId: Id, nodeId: Id): Future[Unit] = {
    val promise: Promise[Unit] = Promise()
    require(operationEndPromises.put((workflowId, nodeId), promise).isEmpty)
    promise.future
  }

  def executionEnded(workflowId: Id, nodeId: Id): Unit = {
    val promise = operationEndPromises.remove((workflowId, nodeId))
    require(promise.isDefined)
    promise.get.success(())
  }

  val customOperationExecutor: CustomOperationExecutor = new CustomOperationExecutor {
    override def execute(workflowId: Id, nodeId: Id): Future[Unit] =
      executionStarted(workflowId, nodeId)
  }
}

object OperationExecutionDispatcher {
  type OperationId = (Workflow.Id, Node.Id)
}
