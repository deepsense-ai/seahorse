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
import io.deepsense.graph.nodestate.NodeStatus
import io.deepsense.models.json.graph.NodeStatusJsonProtocol
import io.deepsense.models.json.workflow.{ExecutionReportJsonProtocol, EntitiesMapJsonProtocol}
import io.deepsense.models.workflows.{EntitiesMap, ExecutionReport}

case class ExecutionStatusMQ(executionReport: ExecutionReport)
  extends WriteMessageMQ {

  override protected def jsMessageType: JsValue = JsString(ExecutionStatusMQ.messageType)

  override protected def jsMessageBody: JsValue = ExecutionStatusMQ.toJsonView(this)
}

object ExecutionStatusMQ
  extends DefaultJsonProtocol
  with FailureDescriptionJsonProtocol
  with NodeStatusJsonProtocol
  with IdJsonProtocol
  with ExecutionReportJsonProtocol {

  def apply(
      nodes: Map[Node.Id, NodeStatus],
      resultEntities: EntitiesMap,
      error: Option[FailureDescription] = None): ExecutionStatusMQ = {
    ExecutionStatusMQ(ExecutionReport(nodes, resultEntities, error))
  }

  val messageType = "executionStatus"

  def toJsonView(executionStatus: ExecutionStatusMQ): JsValue = {
    executionStatus.executionReport.toJson
  }
}
