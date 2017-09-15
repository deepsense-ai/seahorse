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

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.commons.exception.FailureDescription
import io.deepsense.commons.json.{DateTimeJsonProtocol, ExceptionsJsonProtocol, IdJsonProtocol}
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.graph.Graph
import io.deepsense.models.json.graph.GraphJsonProtocol.{GraphReader, GraphWriter}
import io.deepsense.models.json.graph.{GraphKnowledgeJsonProtocol, NodeJsonProtocol, NodeStateJsonProtocol}

trait WorkflowJsonProtocol
  extends DefaultJsonProtocol
  with SprayJsonSupport
  with NodeJsonProtocol
  with NodeStateJsonProtocol
  with GraphKnowledgeJsonProtocol
  with ActionsJsonProtocol
  with IdJsonProtocol
  with ExceptionsJsonProtocol
  with DateTimeJsonProtocol
  with AbstractMetadataJsonProtocol
  with MetadataInferenceResultJsonProtocol
  with InferenceErrorJsonProtocol
  with InferenceWarningJsonProtocol {

  val graphReader: GraphReader
  val inferContext: InferContext

  implicit val graphFormat: JsonFormat[Graph] = new JsonFormat[Graph] {
    override def read(json: JsValue): Graph = json.convertTo[Graph](graphReader)
    override def write(obj: Graph): JsValue = obj.toJson(GraphWriter)
  }

  implicit val workflowErrorFormat = jsonFormat5(FailureDescription.apply)

  // TODO more formats
}
