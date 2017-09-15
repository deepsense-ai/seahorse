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

package io.deepsense.workflowexecutor.communication.message.global

import spray.httpx.SprayJsonSupport
import spray.json.{RootJsonFormat, DefaultJsonProtocol}

import io.deepsense.commons.json.{EnumerationSerializer, IdJsonProtocol}
import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowexecutor.communication.message.global.ReadyMessageType.ReadyMessageType

case class Ready(workflowId: Option[Workflow.Id], content: ReadyContent)

object Ready {
  val seahorseIsReady = Ready(None, ReadyContent(ReadyMessageType.Info, "Seahorse is ready"))
}

trait ReadyJsonProtocol extends IdJsonProtocol with ReadyContentJsonProtocol {

  implicit val readyFormat = jsonFormat2(Ready.apply)
}

trait ReadyContentJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {

  implicit val readyMessageTypeFormat = EnumerationSerializer.jsonEnumFormat(ReadyMessageType)
  implicit val readyContentFormat: RootJsonFormat[ReadyContent] = jsonFormat2(ReadyContent)
}

case class ReadyContent(msgType: ReadyMessageType, text: String)

object ReadyMessageType extends Enumeration {
  type ReadyMessageType = Value
  val Error = Value("error")
  val Warning = Value("warning")
  val Info = Value("info")
}
