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

import spray.json._

import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphWriter
import ai.deepsense.models.workflows.{Workflow, WorkflowMetadata, WorkflowType}


class WorkflowJsonProtocolSpec extends WorkflowJsonTestSupport with WorkflowJsonProtocol {

  "Workflow" should {
    "be serialized to json" in {
      val (workflow, json) = workflowFixture
      workflow.toJson shouldBe json
    }

    "be deserialized from json" in {
      val (workflow, json) = workflowFixture
      json.convertTo[Workflow] shouldBe workflow
    }
  }

  def workflowFixture: (Workflow, JsObject) = {
    val workflow = Workflow(
      WorkflowMetadata(WorkflowType.Batch, "0.4.0"),
      graph,
      JsObject(
        "example" -> JsArray(JsNumber(1), JsNumber(2), JsNumber(3))))
    val workflowJson = JsObject(
      "metadata" -> JsObject(
        "type" -> JsString("batch"),
        "apiVersion" -> JsString("0.4.0")
      ),
      "workflow" -> graph.toJson(GraphWriter),
      "thirdPartyData" -> JsObject(
        "example" -> JsArray(Vector(1, 2, 3).map(JsNumber(_)))
      )
    )
    (workflow, workflowJson)
  }

}
