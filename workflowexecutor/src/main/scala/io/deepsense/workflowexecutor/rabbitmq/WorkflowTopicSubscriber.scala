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
import io.deepsense.models.workflows.Workflow.Id
import io.deepsense.workflowexecutor.{InitWorkflow, WorkflowExecutorActor}
import io.deepsense.workflowexecutor.WorkflowExecutorActor.Messages.UpdateStruct
import io.deepsense.workflowexecutor.communication.message.workflow.{Abort, Init, Launch, UpdateWorkflow}
import io.deepsense.workflowexecutor.communication.mq.MQCommunication

/**
  * Handles messages with topic workflow.${id}. All messages directed to workflows.
  */
case class WorkflowTopicSubscriber(
    executionDispatcher: ActorRef,
    communicationFactory: MQCommunicationFactory,
    sessionId: String) extends Actor with Logging {

  private[this] var publishers: Map[Workflow.Id, ActorRef] = Map()

  override def receive: Receive = {
    case init @ Init(workflowId) =>
      logger.debug(s"INIT! '$workflowId'")
      val publisherPath: ActorPath = getOrCreatePublisher(workflowId)
      executionDispatcher ! InitWorkflow(init, publisherPath)
    case Launch(workflowId, nodesToExecute) =>
      logger.debug(s"LAUNCH! '$workflowId'")
      actorsForWorkflow(workflowId) ! WorkflowExecutorActor.Messages.Launch(nodesToExecute)
    case Abort(workflowId) =>
      logger.debug(s"ABORT! '$workflowId'")
      actorsForWorkflow(workflowId) ! WorkflowExecutorActor.Messages.Abort()
    case UpdateWorkflow(workflowId, workflow) =>
      logger.debug(s"UPDATE STRUCT '$workflowId'")
      actorsForWorkflow(workflowId) ! UpdateStruct(workflow)
  }

  private def getOrCreatePublisher(workflowId: Id): ActorPath = {
    if (!publishers.contains(workflowId)) {
      val publisher: ActorRef = communicationFactory.createPublisher(
        MQCommunication.Topic.workflowPublicationTopic(workflowId, sessionId),
        MQCommunication.Actor.Publisher.workflow(workflowId))
      publishers += (workflowId -> publisher)
    }
    publishers(workflowId).path
  }

  private def actorsForWorkflow(workflowId: Workflow.Id): ActorSelection =
    context.actorSelection(executionDispatcher.path./(workflowId.toString))
}

object WorkflowTopicSubscriber {

  def props(
      executionDispatcher: ActorRef,
      communicationFactory: MQCommunicationFactory,
      sessionId: String): Props = {
    Props(WorkflowTopicSubscriber(executionDispatcher, communicationFactory, sessionId))
  }
}
