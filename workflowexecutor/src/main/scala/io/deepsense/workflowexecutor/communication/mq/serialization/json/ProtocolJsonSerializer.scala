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

import spray.json._

import io.deepsense.commons.utils.Logging
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow.{InferredStateJsonProtocol, ExecutionReportJsonProtocol, WorkflowWithResultsJsonProtocol}
import io.deepsense.models.workflows.{InferredState, WorkflowWithResults}
import io.deepsense.workflowexecutor.communication.message.notebook.{PythonGatewayAddress, PythonGatewayAddressJsonProtocol}
import io.deepsense.workflowexecutor.communication.message.workflow._
import io.deepsense.workflowexecutor.communication.mq.serialization.MessageMQSerializer

case class ProtocolJsonSerializer(graphReader: GraphReader)
  extends MessageMQSerializer
  with ExecutionReportJsonProtocol
  with PythonGatewayAddressJsonProtocol
  with WorkflowWithResultsJsonProtocol
  with InferredStateJsonProtocol
  with Logging {

  import JsonSerialization._

  override def serializeMessage(message: Any): Array[Byte] = {
    messageToJson(message).compactPrint.getBytes(Charset.forName("UTF-8"))
  }

  private def messageToJson(message: Any): JsObject = {
    message match {
      case m: ExecutionStatus =>
        toJsonMQMessage(OutMessages.executionStatus, m.executionReport.toJson)
      case m: WorkflowWithResults =>
        toJsonMQMessage(OutMessages.workflowWithResults, m.toJson)
      case m: InferredState => toJsonMQMessage(OutMessages.inferredState, m.toJson)
      case m: PythonGatewayAddress => toJsonMQMessage(OutMessages.pythonGatewayAddress, m.toJson)
    }
  }

  private def toJsonMQMessage(messageType: String, jsMessageBody: JsValue): JsObject = JsObject(
    messageTypeKey -> JsString(messageType),
    messageBodyKey -> jsMessageBody
  )
}
