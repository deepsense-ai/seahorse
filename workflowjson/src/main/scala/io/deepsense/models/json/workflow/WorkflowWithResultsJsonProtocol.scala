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
import io.deepsense.graph.graphstate._
import io.deepsense.graph.nodestate.NodeState
import io.deepsense.models.entities.Entity
import io.deepsense.models.json.graph.NodeStateJsonProtocol
import io.deepsense.models.workflows._
import io.deepsense.reportlib.model.ReportJsonProtocol

trait WorkflowWithResultsJsonProtocol
  extends WorkflowJsonProtocol
  with NodeStateJsonProtocol
  with EntitiesMapJsonProtocol{



  implicit val executionReportFormat = new JsonFormat[ExecutionReport] {
    val executionReportViewFormat = jsonFormat6(ExecutionReportView)

    override def read(json: JsValue): ExecutionReport = {
      val view = json.convertTo[ExecutionReportView](executionReportViewFormat)
      val state = GraphState.fromString(view.error.get)(view.status)
      ExecutionReport(
        state,
        view.started,
        view.ended,
        view.nodes,
        view.resultEntities
      )
    }

    override def write(obj: ExecutionReport): JsValue = {
      val error = obj.state match {
        case Failed(e) => Some(e)
        case _ => None
      }

      ExecutionReportView(
        obj.state.name,
        obj.started,
        obj.ended,
        error,
        obj.nodes,
        obj.resultEntities
      ).toJson(executionReportViewFormat)
    }


    case class ExecutionReportView(
      status: String,
      started: DateTime,
      ended: DateTime,
      error: Option[FailureDescription],
      nodes: Map[Node.Id, NodeState],
      resultEntities: EntitiesMap)
  }

  implicit val workflowWithResultsFormat =
    jsonFormat(WorkflowWithResults,
      "id", "metadata", "workflow", "thirdPartyData", "executionReport")

}
