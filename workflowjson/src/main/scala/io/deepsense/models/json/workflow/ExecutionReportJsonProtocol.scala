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

package io.deepsense.models.json.workflow

import spray.json._

import io.deepsense.commons.exception.FailureDescription
import io.deepsense.graph.Node
import io.deepsense.graph.nodestate.NodeStatus
import io.deepsense.models.json.graph.NodeStatusJsonProtocol
import io.deepsense.models.workflows._

trait ExecutionReportJsonProtocol
  extends NodeStatusJsonProtocol
  with EntitiesMapJsonProtocol{

  implicit val executionReportJsonFormat: JsonFormat[ExecutionReport] =
      new JsonFormat[ExecutionReport] {
    override def write(executionReport: ExecutionReport): JsValue = JsObject(
      "resultEntities" -> executionReport.resultEntities.toJson,
      "nodes" -> executionReport.nodesStatuses.toJson,
      "error" -> executionReport.error.toJson
    )

    override def read(json: JsValue): ExecutionReport = {
      val fields = json.asJsObject.fields
      val resultEntities: EntitiesMap = fields("resultEntities").convertTo[EntitiesMap]
      val nodes: Map[Node.Id, NodeStatus] = fields("nodes").convertTo[Map[Node.Id, NodeStatus]]
      val error: Option[FailureDescription] = fields("error").convertTo[Option[FailureDescription]]
      ExecutionReport(nodes, resultEntities, error)
    }
  }
}
