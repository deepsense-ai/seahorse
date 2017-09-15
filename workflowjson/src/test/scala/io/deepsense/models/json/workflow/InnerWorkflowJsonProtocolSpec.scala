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

import io.deepsense.deeplang.params.custom.InnerWorkflow
import io.deepsense.graph.Node
import io.deepsense.models.json.graph.GraphJsonProtocol.{GraphReader, GraphWriter}


class InnerWorkflowJsonProtocolSpec extends WorkflowTestSupport with InnerWorkflowJsonProtocol {

  override val graphReader: GraphReader = new GraphReader(catalog)

  "InnerWorkflow" should {
    "be serialized to json" in {
      val (innerWorkflow, json) = innerWorkflowFixture
      innerWorkflow.toJson shouldBe json
    }

    "be deserialized from json" in {
      val (innerWorkflow, json) = innerWorkflowFixture
      json.convertTo[InnerWorkflow] shouldBe innerWorkflow
    }
  }

  def innerWorkflowFixture: (InnerWorkflow, JsObject) = {
    val sourceId = Node.Id.randomId
    val sinkId = Node.Id.randomId
    val innerWorkflow = InnerWorkflow(
      graph,
      JsObject(
        "example" -> JsArray(JsNumber(1), JsNumber(2), JsNumber(3))
      ),
      sourceId,
      sinkId)
    val innerWorkflowJson = JsObject(
      "workflow" -> graph.toJson(GraphWriter),
      "thirdPartyData" -> JsObject(
        "example" -> JsArray(Vector(1, 2, 3).map(JsNumber(_)))
      ),
      "source" -> sourceId.toJson,
      "sink" -> sinkId.toJson
    )
    (innerWorkflow, innerWorkflowJson)
  }
}
