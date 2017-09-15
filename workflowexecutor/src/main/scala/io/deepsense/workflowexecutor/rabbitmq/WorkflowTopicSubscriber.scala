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

package io.deepsense.workflowexecutor.rabbitmq

import akka.actor.{Actor, ActorPath, ActorSelection}

import io.deepsense.commons.utils.Logging
import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowexecutor.WorkflowExecutorActor
import io.deepsense.workflowexecutor.WorkflowExecutorActor.Messages.UpdateStruct
import io.deepsense.workflowexecutor.communication.message.workflow.{Abort, Init, Launch, UpdateWorkflow}

case class WorkflowTopicSubscriber(
  workflowId: Workflow.Id,
  executionDispatcherPath: ActorPath) extends Actor with Logging {

  override def receive: Receive = {
    case request @ Init(id) =>
      logger.debug(s"INIT! '$workflowId'")
      actorsForWorkflow ! WorkflowExecutorActor.Messages.Init()
    case Launch(id, nodesToExecute) =>
      logger.debug(s"LAUNCH! '$id'")
      actorsForWorkflow ! WorkflowExecutorActor.Messages.Launch(nodesToExecute)
    case Abort(id) =>
      logger.debug(s"ABORT! '$id'")
      actorsForWorkflow ! WorkflowExecutorActor.Messages.Abort()
    case UpdateWorkflow(workflow) =>
      logger.debug(s"UPDATE STRUCT '$workflowId'")
      actorsForWorkflow ! UpdateStruct(workflow)
  }

  private val actorsForWorkflow: ActorSelection =
    context.actorSelection(executionDispatcherPath./(workflowId.toString))
}
