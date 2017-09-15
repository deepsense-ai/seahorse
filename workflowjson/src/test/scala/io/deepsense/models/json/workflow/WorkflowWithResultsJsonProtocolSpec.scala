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

import io.deepsense.commons.datetime.DateTimeConverter.{dateTime, toString => dateToString}
import io.deepsense.commons.models.Entity
import io.deepsense.graph.nodestate
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphWriter
import io.deepsense.models.workflows._

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

  private def workflowWithResultsFixture: (WorkflowWithResults, JsObject) = {

    val (executionReport, executionReportJson) = executionReportFixture

    val workflowId = Workflow.Id.randomId

    val workflow = WorkflowWithResults(
      workflowId,
      WorkflowMetadata(WorkflowType.Batch, "0.4.0"),
      graph,
      JsObject("example" -> JsArray(JsNumber(1), JsNumber(2), JsNumber(3))),
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

  private def executionReportFixture: (ExecutionReport, JsObject) = {

    val startDateTime = dateTime(2015, 5, 12, 21, 11, 9)
    val finishDateTime = dateTime(2015, 5, 12, 21, 12, 50)
    val startTimestamp = dateToString(startDateTime)
    val finishTimestamp = dateToString(finishDateTime)

    val entity1Id = Entity.Id.randomId
    val entity2Id = Entity.Id.randomId

    val executionReport = ExecutionReport(
      Map(
        node1.id -> nodestate.Completed(
          startDateTime,
          finishDateTime,
          Seq(entity1Id, entity2Id)
        )
      ),
      EntitiesMap(),
      None
    )
    val executionReportJson = JsObject(
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
