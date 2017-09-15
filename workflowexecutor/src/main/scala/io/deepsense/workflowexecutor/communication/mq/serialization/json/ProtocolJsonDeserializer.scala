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

import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow.WorkflowJsonProtocol
import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowexecutor.communication.message.global._
import io.deepsense.workflowexecutor.communication.message.workflow._
import io.deepsense.workflowexecutor.communication.mq.serialization.MessageMQDeserializer

case class ProtocolJsonDeserializer(graphReader: GraphReader)
  extends MessageMQDeserializer
  with ConnectJsonProtocol
  with LaunchJsonProtocol
  with AbortJsonProtocol
  with InitJsonProtocol
  with GetPythonGatewayAddressJsonProtocol
  with WorkflowJsonProtocol {

  import JsonSerialization._

  def  deserializeMessage(data: Array[Byte]): Any = {
    val json = new String(data, Charset.forName("UTF-8")).parseJson
    val jsObject = json.asJsObject
    deserializeJsonObject(jsObject)
  }

  protected def deserializeJsonObject(jsObject: JsObject): Any = {
    val fields = jsObject.fields
    val messageType = getField(fields, messageTypeKey).convertTo[String]
    val body = getField(fields, messageBodyKey)

    messageType match {
      case InMessageType.connect => body.convertTo[Connect]
      case InMessageType.launch => body.convertTo[Launch]
      case InMessageType.abort => body.convertTo[Abort]
      case InMessageType.init => body.convertTo[Init]
      case InMessageType.getPythonGatewayAddress => body.convertTo[GetPythonGatewayAddress]
      case InMessageType.updateWorkflow => UpdateWorkflow(body.convertTo[Workflow])
    }
  }

  private def getField(fields: Map[String, JsValue], fieldName: String): JsValue = {
    try {
      fields(fieldName)
    } catch {
      case e: NoSuchElementException =>
        throw new DeserializationException(s"Missing field: $fieldName", e)
    }
  }
}
