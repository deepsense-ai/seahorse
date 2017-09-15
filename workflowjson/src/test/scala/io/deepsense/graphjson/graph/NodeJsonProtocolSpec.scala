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

package io.deepsense.graphjson.graph

import org.mockito.Mockito._
import spray.json._

import io.deepsense.commons.json.IdJsonProtocol
import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.parameters.ParametersSchema
import io.deepsense.graph.Node
import io.deepsense.model.json.graph.NodeJsonProtocol

class NodeJsonProtocolSpec extends GraphJsonTestSupport with IdJsonProtocol {

  import io.deepsense.model.json.graph.NodeJsonProtocol._

  "Node with Operation transformed to Json" should {
    val expectedOperationId = DOperation.Id.randomId
    val expectedOperationName = "expectedName"
    val expectedOperationVersion = "0.1.0"
    val dOperation = mock[DOperation]
    val parametersSchema = mock[ParametersSchema]

    when(dOperation.id).thenReturn(expectedOperationId)
    when(dOperation.name).thenReturn(expectedOperationName)
    when(dOperation.version).thenReturn(expectedOperationVersion)
    when(dOperation.parameters).thenReturn(parametersSchema)

    val node = mock[Node]
    val expectedNodeId = Node.Id.randomId
    when(node.operation).thenReturn(dOperation)
    when(node.id).thenReturn(expectedNodeId)
    val nodeJson = node.toJson.asJsObject

    "have correct 'id' field" in {
      nodeJson.fields("id").convertTo[String] shouldBe expectedNodeId.toString
    }

    "have correct 'operation' field" in {
      val operationField = nodeJson.fields("operation").asJsObject
      operationField.fields("id").convertTo[DOperation.Id] shouldBe expectedOperationId
      operationField.fields("name").convertTo[String] shouldBe expectedOperationName
      operationField.fields("version").convertTo[String] shouldBe expectedOperationVersion
    }

    "have 'parameters' field created by internal .toJson method" in {
      verify(parametersSchema).valueToJson
    }
  }
}
