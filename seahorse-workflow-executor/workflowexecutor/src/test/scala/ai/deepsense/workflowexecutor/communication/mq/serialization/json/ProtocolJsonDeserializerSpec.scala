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

package ai.deepsense.workflowexecutor.communication.mq.serialization.json

import java.nio.charset.StandardCharsets

import org.scalatest.mockito.MockitoSugar
import spray.json._

import ai.deepsense.commons.StandardSpec
import ai.deepsense.deeplang.CatalogRecorder
import ai.deepsense.graph.DeeplangGraph
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import ai.deepsense.models.workflows.{Workflow, WorkflowMetadata, WorkflowType}
import ai.deepsense.workflowexecutor.communication.message.workflow.{Abort, Synchronize, UpdateWorkflow}

class ProtocolJsonDeserializerSpec
  extends StandardSpec
  with MockitoSugar {

  "ProtocolJsonDeserializer" should {
    "deserialize Abort messages" in {
      val workflowId = Workflow.Id.randomId

      val rawMessage = JsObject(
        "messageType" -> JsString("abort"),
        "messageBody" -> JsObject(
          "workflowId" -> JsString(workflowId.toString)
        )
      )

      val readMessage: Any = serializeAndRead(rawMessage)
      readMessage shouldBe Abort(workflowId)
    }
    "deserialize UpdateWorkflow messages" in {
      val dOperationsCatalog = CatalogRecorder.resourcesCatalogRecorder.catalogs.operations
      val graphReader = new GraphReader(dOperationsCatalog)
      val protocolDeserializer = ProtocolJsonDeserializer(graphReader)
      val workflowId = Workflow.Id.randomId

      val rawMessage = JsObject(
        "messageType" -> JsString("updateWorkflow"),
        "messageBody" -> JsObject(
          "workflowId" -> JsString(workflowId.toString),
          "workflow" -> JsObject(
            "metadata" -> JsObject(
              "type" -> JsString("batch"),
              "apiVersion" -> JsString("1.0.0")
            ),
            "workflow" -> JsObject(
              "nodes" -> JsArray(),
              "connections" -> JsArray()
            ),
            "thirdPartyData" -> JsObject()
          )
        )
      )

      val readMessage: Any = serializeAndRead(rawMessage, protocolDeserializer)
      readMessage shouldBe UpdateWorkflow(
        workflowId,
        Workflow(WorkflowMetadata(WorkflowType.Batch, "1.0.0"), DeeplangGraph(), JsObject()))
    }

    "deserialize Synchronize messages" in {
      val rawMessage = JsObject(
        "messageType" -> JsString("synchronize"),
        "messageBody" -> JsObject())
      serializeAndRead(rawMessage) shouldBe Synchronize()
    }
  }

  private def serializeAndRead(
      rawMessage: JsObject,
      protocolDeserializer: ProtocolJsonDeserializer =
        ProtocolJsonDeserializer(mock[GraphReader])): Any = {
    val bytes = rawMessage.compactPrint.getBytes(StandardCharsets.UTF_8)
    protocolDeserializer.deserializeMessage(bytes)
  }
}
