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

package ai.deepsense.models.json.workflow

import java.util.UUID

import spray.json._
import ai.deepsense.commons.datetime.DateTimeConverter.{dateTime, toString => dateToString}
import ai.deepsense.commons.models.Entity
import ai.deepsense.graph.nodestate
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphWriter
import ai.deepsense.models.workflows._

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
    val (workflowInfo, workflowInfoJson) = workflowInfoFixture

    val workflowId = Workflow.Id.randomId

    val workflow = WorkflowWithResults(
      workflowId,
      WorkflowMetadata(WorkflowType.Batch, "0.4.0"),
      graph,
      JsObject("example" -> JsArray(JsNumber(1), JsNumber(2), JsNumber(3))),
      executionReport,
      workflowInfo)

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
      "executionReport" -> executionReportJson,
      "workflowInfo" -> workflowInfoJson
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

  private def workflowInfoFixture: (WorkflowInfo, JsObject) = {

    val createdDateTime = dateTime(2015, 5, 12, 21, 11, 9)
    val updatedDateTime = dateTime(2015, 5, 12, 21, 12, 50)
    val createdTimestamp = dateToString(createdDateTime)
    val updatedTimestamp = dateToString(updatedDateTime)

    val workflowId = Workflow.Id.randomId
    val workflowName = " workflow name "
    val description = " some description "
    val ownerId = UUID.randomUUID.toString
    val ownerName = "some@email.com"

    val workflowInfo = WorkflowInfo(
      workflowId,
      workflowName,
      description,
      createdDateTime,
      updatedDateTime,
      ownerId,
      ownerName
    )

    val workflowInfoJson = JsObject(
      "id" -> JsString(workflowId.toString),
      "name" -> JsString(workflowName),
      "description" -> JsString(description),
      "created" -> JsString(createdTimestamp),
      "updated" -> JsString(updatedTimestamp),
      "ownerId" -> JsString(ownerId),
      "ownerName" -> JsString(ownerName)
    )

    (workflowInfo, workflowInfoJson)
  }
}
