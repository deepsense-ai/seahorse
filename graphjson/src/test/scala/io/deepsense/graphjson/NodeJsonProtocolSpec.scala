/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.graphjson

import java.util.UUID

import org.mockito.Mockito._
import spray.json._

import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.parameters.ParametersSchema
import io.deepsense.graph.Node

class NodeJsonProtocolSpec extends GraphJsonTestSupport {

  import NodeJsonProtocol._

  // TODO Test Entity
  "Node with Operation transformed to Json" should {
    val expectedName = "expectedName"
    val dOperation = mock[DOperation]
    val parametersSchema = mock[ParametersSchema]
    when(dOperation.name).thenReturn(expectedName)
    when(dOperation.parameters).thenReturn(parametersSchema)

    val node = mock[Node]
    val expectedNodeId = UUID.randomUUID()
    when(node.operation).thenReturn(dOperation)
    when(node.id).thenReturn(expectedNodeId)
    val nodeJson = node.toJson.asJsObject

    "have correct 'id' field" in {
      nodeJson.fields("id").convertTo[String] shouldBe expectedNodeId.toString
    }

    "have correct 'operation' field" in {
      val operationField = nodeJson.fields("operation").asJsObject
      operationField.fields("name").convertTo[String] shouldBe expectedName
    }

    "have 'parameters' field created by internal .toJson method" in {
      verify(parametersSchema).valueToJson
    }
  }
}
