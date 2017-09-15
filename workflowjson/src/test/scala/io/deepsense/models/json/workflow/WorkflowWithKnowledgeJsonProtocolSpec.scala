/**
 * Copyright 2015, CodiLime Inc.
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

import io.deepsense.deeplang.DKnowledge
import io.deepsense.deeplang.exceptions.DeepLangException
import io.deepsense.deeplang.inference.{InferenceWarning, InferenceWarnings}
import io.deepsense.graph.{NodeInferenceResult, GraphKnowledge}
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphWriter
import io.deepsense.models.workflows._
import spray.json._

class WorkflowWithKnowledgeJsonProtocolSpec extends WorkflowJsonTestSupport
    with WorkflowWithKnowledgeJsonProtocol {

  "WorkflowWithKnowledge" should {
    "be serialized to json" in {
      val (workflow, json) = workflowWithKnowledgeFixture
      workflow.toJson shouldBe json
    }
  }

  def workflowWithKnowledgeFixture: (WorkflowWithKnowledge, JsObject) = {
    val workflowId = Workflow.Id.randomId
    val (graphKnowledge, graphKnowledgeJson) = graphKnowledgeFixture

    val workflow = WorkflowWithKnowledge(
      workflowId,
      WorkflowMetadata(WorkflowType.Batch, "0.4.0"),
      graph,
      ThirdPartyData("{ \"example\": [1, 2, 3] }"),
      graphKnowledge)

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
      "knowledge" -> graphKnowledgeJson
    )

    (workflow, workflowJson)
  }

  def graphKnowledgeFixture: (GraphKnowledge, JsObject) = {
    val graphKnowledge = GraphKnowledge().addInference(
      node1.id,
      NodeInferenceResult(
        Vector(
          DKnowledge(Set(operable)),
          DKnowledge(Set(operable))),
        InferenceWarnings(
          new InferenceWarning("warning1") {},
          new InferenceWarning("warning2") {}),
        Vector(
          new DeepLangException("error1") {},
          new DeepLangException("error2") {}
        )
      )
    )

    val knowledgeJson = JsObject(
      node1.id.toString -> JsObject(
        "ports" -> JsArray(
          JsArray(JsString(operable.getClass.getCanonicalName)),
          JsArray(JsString(operable.getClass.getCanonicalName))
        ),
        "warnings" -> JsArray(
          JsString("warning1"),
          JsString("warning2")
        ),
        "errors" -> JsArray(
          JsString("error1"),
          JsString("error2")
        )
      )
    )

    (graphKnowledge, knowledgeJson)
  }

}
