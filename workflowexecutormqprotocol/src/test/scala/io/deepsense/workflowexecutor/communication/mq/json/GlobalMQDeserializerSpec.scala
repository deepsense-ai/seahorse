/**
 * Copyright 2016, deepsense.io
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

package io.deepsense.workflowexecutor.communication.mq.json

import java.nio.charset.StandardCharsets

import org.scalatest.mockito.MockitoSugar
import spray.json.{JsObject, JsString}

import io.deepsense.commons.StandardSpec
import io.deepsense.workflowexecutor.communication.message.global._
import io.deepsense.workflowexecutor.communication.mq.json.Global.GlobalMQDeserializer

class GlobalMQDeserializerSpec
  extends StandardSpec
  with MockitoSugar {

  "GlobalMQDeserializer" should {
    "deserialize Heartbeat messages" in {
      val workflowId = "foo-workflow"
      val rawMessage = JsObject(
        "messageType" -> JsString("heartbeat"),
        "messageBody" -> JsObject(
          "workflowId" -> JsString(workflowId)))
      serializeAndRead(rawMessage) shouldBe Heartbeat(workflowId)
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
