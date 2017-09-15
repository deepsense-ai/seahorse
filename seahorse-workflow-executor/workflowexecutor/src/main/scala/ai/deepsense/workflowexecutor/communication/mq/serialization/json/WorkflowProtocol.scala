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

package ai.deepsense.workflowexecutor.communication.mq.serialization.json

import spray.json.JsObject

import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import ai.deepsense.models.workflows.{ExecutionReport, InferredState, WorkflowWithResults}
import ai.deepsense.models.json.workflow.ExecutionReportJsonProtocol._
import ai.deepsense.models.json.workflow.{InferredStateJsonProtocol, WorkflowJsonProtocol, WorkflowWithResultsJsonProtocol}
import ai.deepsense.workflowexecutor.communication.message.workflow.AbortJsonProtocol._
import ai.deepsense.workflowexecutor.communication.message.workflow.SynchronizeJsonProtocol._
import ai.deepsense.workflowexecutor.communication.message.workflow._
import ai.deepsense.workflowexecutor.communication.mq.json.Constants.MessagesTypes._
import ai.deepsense.workflowexecutor.communication.mq.json.{DefaultJsonMessageDeserializer, DefaultJsonMessageSerializer, JsonMessageDeserializer, JsonMessageSerializer}

object WorkflowProtocol {
  val abort = "abort"
  val launch = "launch"
  val updateWorkflow = "updateWorkflow"
  val synchronize = "synchronize"

  object AbortDeserializer extends DefaultJsonMessageDeserializer[Abort](abort)

  object SynchronizeDeserializer extends DefaultJsonMessageDeserializer[Synchronize](synchronize)
  object SynchronizeSerializer extends DefaultJsonMessageSerializer[Synchronize](synchronize)

  case class UpdateWorkflowDeserializer(graphReader: GraphReader)
    extends JsonMessageDeserializer
    with UpdateWorkflowJsonProtocol {

    private val defaultDeserializer =
      new DefaultJsonMessageDeserializer[UpdateWorkflow](updateWorkflow)

    override def deserialize: PartialFunction[(String, JsObject), Any] =
      defaultDeserializer.deserialize
  }
}
