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

import org.joda.time.DateTime
import spray.json._

import io.deepsense.commons.exception.FailureDescription
import io.deepsense.graph.Node
import io.deepsense.graph.graphstate.{Failed, GraphState}
import io.deepsense.graph.nodestate.NodeState
import io.deepsense.models.workflows._

trait WorkflowWithSavedResultsJsonProtocol extends WorkflowWithResultsJsonProtocol {

  implicit val executionReportWithIdFormat = new JsonFormat[ExecutionReportWithId] {
    val executionReportWithIdViewFormat = jsonFormat7(ExecutionReportWithIdView)

    override def read(json: JsValue): ExecutionReportWithId = {
      val view = json.convertTo[ExecutionReportWithIdView](executionReportWithIdViewFormat)
      val state = GraphState.fromString(view.error.get)(view.status)
      ExecutionReportWithId(
        view.id,
        state,
        view.started,
        view.ended,
        view.nodes,
        view.resultEntities
      )
    }

    override def write(obj: ExecutionReportWithId): JsValue = {
      val error = obj.status match {
        case Failed(e) => Some(e)
        case _ => None
      }

      ExecutionReportWithIdView(
        obj.id,
        obj.status.name,
        obj.started,
        obj.ended,
        error,
        obj.nodes,
        obj.resultEntities
      ).toJson(executionReportWithIdViewFormat)
    }

    case class ExecutionReportWithIdView(
      id: ExecutionReportWithId.Id,
      status: String,
      started: DateTime,
      ended: DateTime,
      error: Option[FailureDescription],
      nodes: Map[Node.Id, NodeState],
      resultEntities: EntitiesMap
      )
  }

  implicit val workflowWithSavedResultsFormat =
    jsonFormat(WorkflowWithSavedResults.apply,
      "id", "metadata", "workflow", "thirdPartyData", "executionReport")

}
