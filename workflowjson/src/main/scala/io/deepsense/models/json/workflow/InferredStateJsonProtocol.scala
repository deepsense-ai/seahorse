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

import io.deepsense.graph.NodeInferenceResult
import io.deepsense.models.json.graph.GraphKnowledgeJsonProtocol
import io.deepsense.models.workflows.InferredState

/**
 * Deserialization of Knowledge is not supported.
 */
trait InferredStateJsonProtocol
  extends WorkflowJsonProtocol
  with GraphKnowledgeJsonProtocol
  with ExecutionReportJsonProtocol {

  implicit val nodeInferenceResultFormat = jsonFormat3(NodeInferenceResult.apply)

  implicit val knowledgeFormat: RootJsonWriter[InferredState] =
    new RootJsonWriter[InferredState] {
      override def write(knowledge: InferredState): JsValue = {
        JsObject(
          "id" -> knowledge.id.toJson,
          "knowledge" -> knowledge.graphKnowledge.results.toJson,
          "states" -> knowledge.states.toJson)
      }
    }
}
