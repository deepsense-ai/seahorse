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

import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphWriter
import ai.deepsense.models.workflows._
import spray.json._

class WorkflowWithVariablesJsonProtocolSpec extends WorkflowJsonTestSupport
    with WorkflowWithVariablesJsonProtocol {

  "WorkflowWithVariables" should {

    "be serialized to json" in {
      val (workflow, json) = workflowWithVariablesFixture
      workflow.toJson shouldBe json
    }

    "be deserialized from json" in {
      val (workflow, json) = workflowWithVariablesFixture
      json.convertTo[WorkflowWithVariables] shouldBe workflow
    }
  }

  def workflowWithVariablesFixture: (WorkflowWithVariables, JsObject) = {

    val workflowId = Workflow.Id.randomId

    val workflow = WorkflowWithVariables(
      workflowId,
      WorkflowMetadata(WorkflowType.Batch, "0.4.0"),
      graph,
      JsObject("example" -> JsArray(JsNumber(1), JsNumber(2), JsNumber(3))),
      Variables())

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
      "variables" -> JsObject()
    )

    (workflow, workflowJson)
  }

}
