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

import io.deepsense.graph.{NodeInferenceResult, GraphKnowledge, Graph}
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphWriter
import io.deepsense.models.json.graph.GraphKnowledgeJsonProtocol
import io.deepsense.models.workflows.{ThirdPartyData, WorkflowMetadata, Workflow, WorkflowWithKnowledge}

import spray.json._

/**
 * Deserialization of WorkflowWithKnowledge is not supported.
 */
trait WorkflowWithKnowledgeJsonProtocol extends WorkflowJsonProtocol
    with GraphKnowledgeJsonProtocol {

  implicit val nodeInferenceResultFormat = jsonFormat3(NodeInferenceResult.apply)

  implicit val workflowWithKnowledgeFormat: RootJsonFormat[WorkflowWithKnowledge] =
    new RootJsonFormat[WorkflowWithKnowledge] {
      override def read(json: JsValue): WorkflowWithKnowledge = ???

      override def write(workflow: WorkflowWithKnowledge): JsValue = {
        JsObject(
          "id" -> workflow.id.toJson,
          "metadata" -> workflow.metadata.toJson,
          "workflow" -> workflow.graph.toJson(GraphWriter),
          "thirdPartyData" -> workflow.thirdPartyData.toJson,
          "knowledge" -> workflow.knowledge.results.toJson)
      }
    }
}
