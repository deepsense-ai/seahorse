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

package ai.deepsense.models.json.graph

import ai.deepsense.commons.json.IdJsonProtocol
import ai.deepsense.graph.DeeplangGraph.DeeplangNode
import spray.json._

trait NodeJsonProtocol extends DefaultJsonProtocol with IdJsonProtocol {

  import OperationJsonProtocol.DOperationWriter

  implicit object NodeWriter extends JsonWriter[DeeplangNode] {
    override def write(node: DeeplangNode): JsValue = JsObject(
      Map(NodeJsonProtocol.Id -> node.id.toJson) ++
        node.value.toJson.asJsObject.fields)
  }
}

object NodeJsonProtocol extends NodeJsonProtocol {
  val Id = "id"
}
