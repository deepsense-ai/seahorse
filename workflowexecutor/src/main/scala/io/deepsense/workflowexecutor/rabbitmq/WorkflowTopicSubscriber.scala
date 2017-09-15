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

import akka.actor._

import io.deepsense.commons.utils.Logging
import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowexecutor.WorkflowExecutorActor.Messages.{Init, UpdateStruct}
import io.deepsense.workflowexecutor.communication.message.workflow.{Abort, Launch, UpdateWorkflow}
import io.deepsense.workflowexecutor.executor.Executor
import io.deepsense.workflowexecutor.{SessionWorkflowExecutorActorProvider, WorkflowExecutorActor}

/**
  * Handles messages with topic workflow.${id}. All messages directed to workflows.
  */
case class WorkflowTopicSubscriber(
      actorProvider: SessionWorkflowExecutorActorProvider,
      sessionId: String,
      workflowId: Workflow.Id)
    extends Actor
    with Logging
    with Executor {

  private val executorActor: ActorRef = actorProvider.provide(context, workflowId)

  override def receive: Receive = {
    case Init() =>
      logger.debug(s"Initializing SessionWorkflowExecutorActor for workflow '$workflowId'")
      executorActor ! Init()
    case Launch(id, nodesToExecute) if id == workflowId =>
      logger.debug(s"LAUNCH! '$workflowId'")
      executorActor ! WorkflowExecutorActor.Messages.Launch(nodesToExecute)
    case Abort(id) if id == workflowId =>
      logger.debug(s"ABORT! '$workflowId'")
      executorActor ! WorkflowExecutorActor.Messages.Abort()
    case UpdateWorkflow(id, workflow) if id == workflowId =>
      logger.debug(s"UPDATE STRUCT '$workflowId'")
      executorActor ! UpdateStruct(workflow)
    case x =>
      logger.error(s"Unexpected '$x' from '${sender()}'!")
  }
}

object WorkflowTopicSubscriber {
  def props(
      actorProvider: SessionWorkflowExecutorActorProvider,
      sessionId: String,
      workflowId: Workflow.Id): Props = {
    Props(WorkflowTopicSubscriber(
      actorProvider,
      sessionId,
      workflowId))
  }
}
