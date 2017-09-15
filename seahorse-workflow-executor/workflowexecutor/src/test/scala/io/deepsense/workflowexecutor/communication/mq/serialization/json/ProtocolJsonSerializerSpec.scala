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

package io.deepsense.workflowexecutor.communication.mq.serialization.json

import java.nio.charset.Charset

import org.scalatest.mockito.MockitoSugar
import spray.json._

import io.deepsense.commons.StandardSpec
import io.deepsense.commons.models.Entity
import io.deepsense.deeplang.DOperable
import io.deepsense.deeplang.doperables.ColumnsFilterer
import io.deepsense.graph._
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow.{ExecutionReportJsonProtocol, InferredStateJsonProtocol, WorkflowWithResultsJsonProtocol}
import io.deepsense.models.workflows._
import io.deepsense.reportlib.model.factory.ReportContentTestFactory
import io.deepsense.workflowexecutor.communication.message.global._
import io.deepsense.workflowexecutor.communication.message.workflow.Synchronize

class ProtocolJsonSerializerSpec
  extends StandardSpec
  with MockitoSugar
  with WorkflowWithResultsJsonProtocol
  with InferredStateJsonProtocol
  with HeartbeatJsonProtocol {

  override val graphReader: GraphReader = mock[GraphReader]

  "ProtocolJsonSerializer" should {
    val protocolJsonSerializer = ProtocolJsonSerializer(graphReader)


    "serialize Synchronize messages" in {
      protocolJsonSerializer.serializeMessage(Synchronize()) shouldBe
        expectedSerializationResult("synchronize", JsObject())
    }

  }

  private def expectedSerializationResult(messageType: String, jsonObject: JsValue): Array[Byte] = {
    JsObject(
      "messageType" -> JsString(messageType),
      "messageBody" -> jsonObject
    ).compactPrint.getBytes(Charset.forName("UTF-8"))
  }

}
