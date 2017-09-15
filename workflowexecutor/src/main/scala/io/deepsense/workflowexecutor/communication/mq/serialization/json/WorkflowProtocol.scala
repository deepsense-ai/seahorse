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

package io.deepsense.workflowexecutor.communication.mq.serialization.json

import spray.json.JsObject

import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.workflows.{ExecutionReport, InferredState, WorkflowWithResults}
import io.deepsense.models.json.workflow.ExecutionReportJsonProtocol._
import io.deepsense.models.json.workflow.{InferredStateJsonProtocol, WorkflowJsonProtocol, WorkflowWithResultsJsonProtocol}
import io.deepsense.workflowexecutor.communication.message.workflow.AbortJsonProtocol._
import io.deepsense.workflowexecutor.communication.message.workflow.LaunchJsonProtocol._
import io.deepsense.workflowexecutor.communication.message.workflow.{Abort, Launch, UpdateWorkflow, UpdateWorkflowJsonProtocol}
import io.deepsense.workflowexecutor.communication.mq.json.{DefaultJsonMessageDeserializer, DefaultJsonMessageSerializer, JsonMessageDeserializer, JsonMessageSerializer}

object WorkflowProtocol {
  val abort = "abort"
  val executionReport = "executionStatus" // 'status' for backward compatibility
  val launch = "launch"
  val updateWorkflow = "updateWorkflow"
  val inferredState = "inferredState"
  val workflowWithResults = "workflowWithResults"


  object AbortDeserializer extends DefaultJsonMessageDeserializer[Abort](abort)

  object LaunchDeserializer extends DefaultJsonMessageDeserializer[Launch](launch)

  object ExecutionStatusSerializer
    extends DefaultJsonMessageSerializer[ExecutionReport](executionReport)

  case class UpdateWorkflowDeserializer(graphReader: GraphReader)
    extends JsonMessageDeserializer
    with UpdateWorkflowJsonProtocol {

    private val defaultDeserializer =
      new DefaultJsonMessageDeserializer[UpdateWorkflow](updateWorkflow)

    override def deserialize: PartialFunction[(String, JsObject), Any] =
      defaultDeserializer.deserialize
  }

  case class InferredStateSerializer(graphReader: GraphReader)
    extends JsonMessageSerializer
    with InferredStateJsonProtocol {

    private val defaultSerializer =
      new DefaultJsonMessageSerializer[InferredState](inferredState)

    override def serialize: PartialFunction[Any, JsObject] = defaultSerializer.serialize
  }

  case class WorkflowWithResultsSerializer(graphReader: GraphReader)
    extends JsonMessageSerializer
      with WorkflowWithResultsJsonProtocol {

    private val defaultSerializer =
      new DefaultJsonMessageSerializer[WorkflowWithResults](workflowWithResults)

    override def serialize: PartialFunction[Any, JsObject] = defaultSerializer.serialize
  }
}
