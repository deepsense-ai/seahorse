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

import ai.deepsense.graph.{GraphKnowledge, Node, NodeInferenceResult}
import ai.deepsense.models.json.graph.DKnowledgeJsonProtocol
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import ai.deepsense.models.workflows.{ExecutionReport, InferredState}

trait InferredStateJsonProtocol extends WorkflowJsonProtocol
  with DKnowledgeJsonProtocol
  with ExecutionReportJsonProtocol
  with InferenceWarningsJsonProtocol {

  import InferredStateJsonProtocol._

  implicit val nodeInferenceResultFormat = jsonFormat3(NodeInferenceResult.apply)

  implicit val inferredStateWriter: RootJsonWriter[InferredState] =
    new RootJsonWriter[InferredState] {
      override def write(inferredState: InferredState): JsValue = {
        JsObject(
          idFieldName -> inferredState.id.toJson,
          knowledgeFieldName -> inferredState.graphKnowledge.results.toJson,
          statesFieldName -> inferredState.states.toJson)
      }
    }
  implicit val inferredStateReader: RootJsonReader[InferredState] =
    new RootJsonReader[InferredState] {
      override def read(json: JsValue): InferredState = {
        val fields = json.asJsObject.fields
        val inferenceResults = fields(knowledgeFieldName).convertTo[Map[Node.Id, NodeInferenceResult]]
        InferredState(
          fields(idFieldName).convertTo[Node.Id],
          GraphKnowledge(inferenceResults),
          fields(statesFieldName).convertTo[ExecutionReport]
        )
      }
    }
}

object InferredStateJsonProtocol {

  def apply(_graphReader: GraphReader): InferredStateJsonProtocol = new InferredStateJsonProtocol {
    override val graphReader = _graphReader
  }

  val idFieldName = "id"
  val knowledgeFieldName = "knowledge"
  val statesFieldName = "states"
}
