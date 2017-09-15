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

object JsonSerialization {

  val messageTypeKey = "messageType"
  val messageBodyKey = "messageBody"

  object InMessageType {
    val connect = "connect"
    val getPythonGatewayAddress = "getPythonGatewayAddress"
    val abort = "abort"
    val init = "init"
    val launch = "launch"
    val updateWorkflow = "updateWorkflow"
  }

  object OutMessages {
    val pythonGatewayAddress = "pythonGatewayAddress"
    val executionStatus = "executionStatus"
    val workflowWithResults = "workflowWithResults"
    val inferredState = "inferredState"
    val ready = "ready"
  }
}
