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

package io.deepsense.workflowexecutor.communication

import spray.json._

import io.deepsense.commons.exception.FailureDescription
import io.deepsense.commons.exception.json.FailureDescriptionJsonProtocol
import io.deepsense.commons.json.IdJsonProtocol
import io.deepsense.graph.Node
import io.deepsense.graph.nodestate.NodeState
import io.deepsense.models.json.graph.NodeStateJsonProtocol
import io.deepsense.models.json.workflow.EntitiesMapJsonProtocol
import io.deepsense.models.workflows.EntitiesMap

case class ExecutionStatus(
    nodes: Map[Node.Id, NodeState],
    resultEntities: EntitiesMap,
    executionFailure: Option[FailureDescription] = None)
  extends WriteMessageMQ {

  override protected def jsMessageType: JsValue = JsString(ExecutionStatus.messageType)

  override protected def jsMessageBody: JsValue = ExecutionStatus.toJsonView(this)
}

object ExecutionStatus
  extends DefaultJsonProtocol
  with FailureDescriptionJsonProtocol
  with NodeStateJsonProtocol
  with IdJsonProtocol
  with EntitiesMapJsonProtocol {

  val messageType = "executionStatus"
  val protocol = jsonFormat3(JsonExecutionStatus.apply)

  def toJsonView(executionStatus: ExecutionStatus): JsValue = {
    JsonExecutionStatus(
      executionStatus.executionFailure,
      executionStatus.nodes,
      executionStatus.resultEntities
    ).toJson(protocol)
  }

  case class JsonExecutionStatus(
    error: Option[FailureDescription],
    nodes: Map[Node.Id, NodeState],
    resultEntities: EntitiesMap)
}
