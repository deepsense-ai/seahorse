/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.json

import org.joda.time.DateTime
import spray.json._

import io.deepsense.graph.{State, Status}
import io.deepsense.models.entities.Entity
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphWriter
import io.deepsense.models.workflows._
import io.deepsense.workflowmanager.model.{ExecutionReportWithId, WorkflowWithSavedResults}

class WorkflowWithSavedResultsJsonProtocolSpec extends WorkflowJsonTestSupport
    with WorkflowWithSavedResultsJsonProtocol {

  "WorkflowWithSavedResults" should {

    "be serialized to json" in {
      val (workflow, json) = workflowWithSavedResultsFixture
      workflow.toJson shouldBe json
    }

    "be deserialized from json" in {
      val (workflow, json) = workflowWithSavedResultsFixture
      json.convertTo[WorkflowWithSavedResults] shouldBe workflow
    }
  }

  def workflowWithSavedResultsFixture: (WorkflowWithSavedResults, JsObject) = {

    val (executionReport, executionReportJson) = executionReportFixture

    val workflowId = Workflow.Id.randomId

    val workflow = WorkflowWithSavedResults(
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

  def executionReportFixture: (ExecutionReportWithId, JsObject) = {

    val startTimestamp = "2015-05-12T21:11:09.000Z"
    val finishTimestamp = "2015-05-12T21:12:50.000Z"

    val entity1Id = Entity.Id.randomId
    val entity2Id = Entity.Id.randomId

    val executionReportId: ExecutionReportWithId.Id = ExecutionReportWithId.Id.randomId

    val executionReport = ExecutionReportWithId(
      executionReportId,
      Status.Completed,
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
      "id" -> JsString(executionReportId.toString()),
      "status" -> JsString("COMPLETED"),
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
