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

package io.deepsense.workflowexecutor.communication

import java.nio.charset.StandardCharsets

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import spray.json._

import io.deepsense.commons.StandardSpec
import io.deepsense.graph.DirectedGraph
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.workflows.Workflow

class ProtocolDeserializerSpec
  extends StandardSpec
  with MockitoSugar {

  "ProtocolDeserializer" should {
    "deserialize Launch messages" in {
      val graphReader = mock[GraphReader]
      val graph = mock[DirectedGraph]("expectedGraph")
      when(graphReader.read(any())).thenReturn(graph)
      val graphJs = JsObject("graphJs" -> JsString(""))
      val protocolDeserializer = ProtocolDeserializer(graphReader)

      val workflowId = Workflow.Id.randomId
      val nodesToExecute = Vector(Workflow.Id.randomId, Workflow.Id.randomId, Workflow.Id.randomId)
      val jsNodesToExecute = JsArray(nodesToExecute.map(id => JsString(id.toString)))

      val rawMessage = JsObject(
        "messageType" -> JsString("launch"),
        "messageBody" -> JsObject(
          "workflowId" -> JsString(workflowId.toString),
          "workflow" -> graphJs,
          "nodesToExecute" -> jsNodesToExecute
        )
      )

      val readMessage: ReadMessageMQ = serializeAndRead(protocolDeserializer, rawMessage)

      verify(graphReader).read(graphJs)

      readMessage shouldBe Launch(
        workflowId,
        graph,
        nodesToExecute)
    }
    "deserialize Abort messages" in {
      val protocolDeserializer = ProtocolDeserializer(mock[GraphReader])
      val workflowId = Workflow.Id.randomId

      val rawMessage = JsObject(
        "messageType" -> JsString("abort"),
        "messageBody" -> JsObject(
          "workflowId" -> JsString(workflowId.toString)
        )
      )

      val readMessage: ReadMessageMQ = serializeAndRead(protocolDeserializer, rawMessage)
      readMessage shouldBe Abort(workflowId)
    }
  }

  def serializeAndRead(
      protocolDeserializer: ProtocolDeserializer,
      rawMessage: JsObject): ReadMessageMQ = {
    val bytes = rawMessage.compactPrint.getBytes(StandardCharsets.UTF_8)
    protocolDeserializer.deserializeMessage(bytes)
  }
}
