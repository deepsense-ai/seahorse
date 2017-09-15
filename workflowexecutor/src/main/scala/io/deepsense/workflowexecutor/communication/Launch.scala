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

import spray.json._

import io.deepsense.commons.BuildInfo
import io.deepsense.commons.utils.Version
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow.{WorkflowVersionUtil, WorkflowWithVariablesJsonProtocol}
import io.deepsense.models.workflows.WorkflowWithVariables

case class Launch(workflow: WorkflowWithVariables) extends MessageMQ {
  override protected def jsMessageType: JsValue = ???

  override protected def jsMessageBody: JsValue = ???
}

object Launch {
  val messageType: String = "launch"
}
