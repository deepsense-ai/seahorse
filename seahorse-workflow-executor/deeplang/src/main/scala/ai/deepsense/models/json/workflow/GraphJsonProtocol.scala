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

import spray.json.{JsValue, JsonFormat}

import ai.deepsense.graph.DeeplangGraph
import ai.deepsense.models.json.graph.GraphJsonProtocol.{GraphReader, GraphWriter}

trait GraphJsonProtocol {

  protected def graphReader: GraphReader

  implicit def graphFormat: JsonFormat[DeeplangGraph] = new JsonFormat[DeeplangGraph] {
    override def read(json: JsValue): DeeplangGraph = json.convertTo[DeeplangGraph](graphReader)
    override def write(obj: DeeplangGraph): JsValue = obj.toJson(GraphWriter)
  }
}
