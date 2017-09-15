/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.graphjson

import java.util.UUID

import org.mockito.Mockito._
import spray.json._

import io.deepsense.commons.json.IdJsonProtocol
import io.deepsense.commons.models
import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.parameters.ParametersSchema
import io.deepsense.graph.Node

class NodeJsonProtocolSpec extends GraphJsonTestSupport with IdJsonProtocol {

  import NodeJsonProtocol._

  "Node with Operation transformed to Json" should {
    val expectedOperationId = models.Id.randomId
    val expectedOperationName = "expectedName"
    val expectedOperationVersion = "0.1.0"
    val dOperation = mock[DOperation]
    val parametersSchema = mock[ParametersSchema]

    when(dOperation.id).thenReturn(expectedOperationId)
    when(dOperation.name).thenReturn(expectedOperationName)
    when(dOperation.version).thenReturn(expectedOperationVersion)
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
      operationField.fields("id").convertTo[DOperation.Id] shouldBe expectedOperationId
      operationField.fields("name").convertTo[String] shouldBe expectedOperationName
      operationField.fields("version").convertTo[String] shouldBe expectedOperationVersion
    }

    "have 'parameters' field created by internal .toJson method" in {
      verify(parametersSchema).valueToJson
    }
  }
}
