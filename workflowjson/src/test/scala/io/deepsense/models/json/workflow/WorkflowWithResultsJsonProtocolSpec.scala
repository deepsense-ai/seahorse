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

import io.deepsense.graph.{State, Status}
import io.deepsense.models.entities.Entity
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphWriter
import io.deepsense.models.workflows._
import org.joda.time.DateTime
import spray.json._

class WorkflowWithResultsJsonProtocolSpec extends WorkflowJsonTestSupport
    with WorkflowWithResultsJsonProtocol {

  "WorkflowWithResults" should {

    "be serialized to json" in {
      val (workflow, json) = workflowWithResultsFixture
      workflow.toJson shouldBe json
    }

    "be deserialized from json" in {
      val (workflow, json) = workflowWithResultsFixture
      json.convertTo[WorkflowWithResults] shouldBe workflow
    }
  }

  def workflowWithResultsFixture: (WorkflowWithResults, JsObject) = {

    val (executionReport, executionReportJson) = executionReportFixture

    val workflowId = Workflow.Id.randomId

    val workflow = WorkflowWithResults(
      workflowId,
      WorkflowMetadata(WorkflowType.Batch, "0.4.0"),
      graph,
      ThirdPartyData("{ \"example\": [1, 2, 3] }"),
      executionReport)

    val workflowJson = JsObject(
      "id" -> JsString(workflowId.toString),
      "metadata" -> JsObject(
        "type" -> JsString("batch"),
        "apiVersion" -> JsString("0.4.0")
      ),
      "workflow" -> graph.toJson(GraphWriter),
      "thirdPartyData" -> JsObject(
        "example" -> JsArray(Vector(1, 2, 3).map(JsNumber(_)))
      ),
      "executionReport" -> executionReportJson
    )

    (workflow, workflowJson)
  }

  def executionReportFixture: (ExecutionReport, JsObject) = {

    val startTimestamp = "2015-05-12T21:11:09.000Z"
    val finishTimestamp = "2015-05-12T21:12:50.000Z"

    val workflowStartTimestamp = "2014-01-13T21:32:09.000Z"
    val workflowFinishTimestamp = "2015-12-14T23:45:40.000Z"

    val entity1Id = Entity.Id.randomId
    val entity2Id = Entity.Id.randomId

    val executionReport = ExecutionReport(
      Status.Completed,
      DateTime.parse(workflowStartTimestamp),
      DateTime.parse(workflowFinishTimestamp),
      None,
      Map(
        node1.id -> State(
          Status.Completed,
          Some(DateTime.parse(startTimestamp)),
          Some(DateTime.parse(finishTimestamp)),
          progress = None,
          Some(Seq(entity1Id, entity2Id)),
          None
        )
      ),
      EntitiesMap()
    )
    val executionReportJson = JsObject(
      "status" -> JsString("COMPLETED"),
      "started" -> JsString(workflowStartTimestamp),
      "ended" -> JsString(workflowFinishTimestamp),
      "error" -> JsNull,
      "nodes" -> JsObject(
        node1.id.toString -> JsObject(
          "status" -> JsString("COMPLETED"),
          "started" -> JsString(startTimestamp),
          "ended" -> JsString(finishTimestamp),
          "results" -> JsArray(
            JsString(entity1Id.toString),
            JsString(entity2Id.toString)
          ),
          "error" -> JsNull
        )
      ),
      "resultEntities" -> JsObject()
    )

    (executionReport, executionReportJson)
  }

}
