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

import spray.json.{RootJsonFormat, DefaultJsonProtocol, JsObject}

import io.deepsense.commons.utils.{Logging, Version}
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow.WorkflowVersionUtil
import io.deepsense.workflowexecutor.communication.ConnectJsonProtocol._

case class ProtocolDeserializer(
    override val graphReader: GraphReader,
    override val currentVersion: Version)
  extends MQMessageDeserializer
  with Logging
  with DefaultJsonProtocol
  with WorkflowVersionUtil {

  def launchReader: RootJsonFormat[Launch] = jsonFormat1(Launch.apply)

  override protected def deserializeJson(jsObject: JsObject): MessageMQ = {
    val fields = jsObject.fields
    val messageType: String = fields(MessageMQ.messageTypeKey).convertTo[String]
    messageType match {
      case Connect.messageType => fields(MessageMQ.messageBodyKey).convertTo[Connect]
      case Launch.messageType =>
        fields(MessageMQ.messageBodyKey).convertTo[Launch](launchReader)
    }
  }
}
