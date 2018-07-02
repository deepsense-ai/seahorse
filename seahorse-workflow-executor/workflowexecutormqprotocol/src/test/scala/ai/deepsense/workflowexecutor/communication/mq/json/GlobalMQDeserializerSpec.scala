/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.workflowexecutor.communication.mq.json

import java.nio.charset.StandardCharsets

import org.scalatest.mockito.MockitoSugar
import spray.json.{JsArray, JsNull, JsObject, JsString}
import ai.deepsense.commons.StandardSpec
import ai.deepsense.models.workflows.Workflow
import ai.deepsense.workflowexecutor.communication.message.global._
import ai.deepsense.workflowexecutor.communication.mq.json.Global.GlobalMQDeserializer

class GlobalMQDeserializerSpec
  extends StandardSpec
  with MockitoSugar {

  "GlobalMQDeserializer" should {
    "deserialize Launch messages" in {
      val workflowId = Workflow.Id.randomId
      val nodesToExecute = Vector(Workflow.Id.randomId, Workflow.Id.randomId, Workflow.Id.randomId)
      val jsNodesToExecute = JsArray(nodesToExecute.map(id => JsString(id.toString)))

      val rawMessage = JsObject(
        "messageType" -> JsString("launch"),
        "messageBody" -> JsObject(
          "workflowId" -> JsString(workflowId.toString),
          "nodesToExecute" -> jsNodesToExecute
        )
      )

      val readMessage: Any = serializeAndRead(rawMessage)
      readMessage shouldBe Launch(workflowId, nodesToExecute.toSet)
    }

    "deserialize Heartbeat messages" in {
      val workflowId = "foo-workflow"
      val rawMessage = JsObject(
        "messageType" -> JsString("heartbeat"),
        "messageBody" -> JsObject(
          "workflowId" -> JsString(workflowId),
          "sparkUiAddress" -> JsNull))
      serializeAndRead(rawMessage) shouldBe Heartbeat(workflowId, None)
    }
    "deserialize PoisonPill messages" in {
      val rawMessage = JsObject(
        "messageType" -> JsString("poisonPill"),
        "messageBody" -> JsObject())
      serializeAndRead(rawMessage) shouldBe PoisonPill()
    }
    "deserialize Ready messages" in {
      val sessionId = "foo-session"
      val rawMessage = JsObject(
        "messageType" -> JsString("ready"),
        "messageBody" -> JsObject(
          "sessionId" -> JsString(sessionId)))
      serializeAndRead(rawMessage) shouldBe Ready(sessionId)
    }
  }

  private def serializeAndRead(
    rawMessage: JsObject): Any = {
    val bytes = rawMessage.compactPrint.getBytes(StandardCharsets.UTF_8)
    GlobalMQDeserializer.deserializeMessage(bytes)
  }
}
