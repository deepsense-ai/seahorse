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

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.commons.exception.json.FailureDescriptionJsonProtocol
import io.deepsense.commons.json.{DateTimeJsonProtocol, EnumerationSerializer, IdJsonProtocol}
import io.deepsense.deeplang.params.custom.InnerWorkflow
import io.deepsense.graph.DeeplangGraph
import io.deepsense.models.json.graph.GraphJsonProtocol.{GraphReader, GraphWriter}
import io.deepsense.models.json.graph.{DKnowledgeJsonProtocol, NodeJsonProtocol, NodeStatusJsonProtocol}
import io.deepsense.models.workflows._

trait InnerWorkflowJsonProtocol
  extends DefaultJsonProtocol
  with SprayJsonSupport
  with NodeJsonProtocol
  with NodeStatusJsonProtocol
  with DKnowledgeJsonProtocol
  with IdJsonProtocol
  with DateTimeJsonProtocol {

  val graphReader: GraphReader

  implicit val graphFormat: JsonFormat[DeeplangGraph] = new JsonFormat[DeeplangGraph] {
    override def read(json: JsValue): DeeplangGraph = json.convertTo[DeeplangGraph](graphReader)
    override def write(obj: DeeplangGraph): JsValue = obj.toJson(GraphWriter)
  }

  implicit val innerWorkflowFormat = jsonFormat(
    InnerWorkflow.apply, "workflow", "thirdPartyData", "source", "sink")

}
