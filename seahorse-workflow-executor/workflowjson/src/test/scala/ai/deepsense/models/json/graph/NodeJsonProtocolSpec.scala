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

import org.mockito.Mockito._
import spray.json._

import ai.deepsense.commons.json.IdJsonProtocol
import ai.deepsense.deeplang.DOperation
import ai.deepsense.graph.DeeplangGraph.DeeplangNode
import ai.deepsense.graph.Node

class NodeJsonProtocolSpec extends GraphJsonTestSupport with IdJsonProtocol {

  import ai.deepsense.models.json.graph.NodeJsonProtocol._

  "Node with Operation transformed to Json" should {
    val expectedOperationId = DOperation.Id.randomId
    val expectedOperationName = "expectedName"
    val dOperation = mock[DOperation]

    when(dOperation.id).thenReturn(expectedOperationId)
    when(dOperation.name).thenReturn(expectedOperationName)

    val node = mock[DeeplangNode]
    val expectedNodeId = Node.Id.randomId
    when(node.value).thenReturn(dOperation)
    when(node.id).thenReturn(expectedNodeId)
    val nodeJson = node.toJson.asJsObject

    "have correct 'id' field" in {
      nodeJson.fields("id").convertTo[String] shouldBe expectedNodeId.toString
    }

    "have correct 'operation' field" in {
      val operationField = nodeJson.fields("operation").asJsObject
      operationField.fields("id").convertTo[DOperation.Id] shouldBe expectedOperationId
      operationField.fields("name").convertTo[String] shouldBe expectedOperationName
    }
  }
}
