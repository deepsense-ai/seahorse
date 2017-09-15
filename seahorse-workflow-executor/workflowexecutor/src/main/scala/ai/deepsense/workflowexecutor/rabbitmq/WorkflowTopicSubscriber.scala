/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.workflowexecutor.rabbitmq

import akka.actor._

import ai.deepsense.commons.utils.Logging
import ai.deepsense.models.workflows.Workflow
import ai.deepsense.sparkutils.AkkaUtils
import ai.deepsense.workflowexecutor.communication.message.global.Launch
import ai.deepsense.workflowexecutor.communication.message.{global, workflow}
import ai.deepsense.workflowexecutor.executor.Executor
import ai.deepsense.workflowexecutor.{SessionWorkflowExecutorActorProvider, WorkflowExecutorActor}

/**
  * Handles messages with topic workflow.&#36;{id}. All messages directed to workflows.
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
    case WorkflowExecutorActor.Messages.Init() =>
      logger.debug(s"Initializing SessionWorkflowExecutorActor for workflow '$workflowId'")
      executorActor ! WorkflowExecutorActor.Messages.Init()
    case global.Launch(id, nodesToExecute) if id == workflowId =>
      logger.debug(s"LAUNCH! '$workflowId'")
      executorActor ! WorkflowExecutorActor.Messages.Launch(nodesToExecute)
    case workflow.Abort(id) if id == workflowId =>
      logger.debug(s"ABORT! '$workflowId'")
      executorActor ! WorkflowExecutorActor.Messages.Abort()
    case workflow.UpdateWorkflow(id, workflow) if id == workflowId =>
      logger.debug(s"UPDATE STRUCT '$workflowId'")
      executorActor ! WorkflowExecutorActor.Messages.UpdateStruct(workflow)
    case workflow.Synchronize() =>
      logger.debug(s"Got Synchronize() request for workflow '$workflowId'")
      executorActor ! WorkflowExecutorActor.Messages.Synchronize()
    case global.PoisonPill() =>
      logger.info("Got PoisonPill! Terminating Actor System!")
      AkkaUtils.terminate(context.system)
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
