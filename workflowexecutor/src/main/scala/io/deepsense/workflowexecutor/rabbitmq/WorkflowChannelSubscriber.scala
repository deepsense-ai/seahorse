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

import akka.actor.{ActorSelection, Actor, ActorRef}

import io.deepsense.commons.utils.Logging
import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowexecutor.WorkflowExecutorActor
import io.deepsense.workflowexecutor.communication.{StatusRequest, Abort, Launch}

case class WorkflowChannelSubscriber(
  executionDispatcher: ActorRef) extends Actor with Logging {

  override def receive: Receive = {
    case request @ StatusRequest(workflowId) =>
      logger.debug(s"STATUS REQUEST! '$workflowId'")
      actorsForWorkflow(workflowId) ! request
    case Launch(id, directedGraph, nodesToExecute) =>
      logger.debug(s"LAUNCH! '$id' -> $directedGraph")
      actorsForWorkflow(id) ! WorkflowExecutorActor.Messages.Launch(directedGraph, nodesToExecute)
    case Abort(id) =>
      logger.debug(s"ABORT! '$id'")
      actorsForWorkflow(id) ! WorkflowExecutorActor.Messages.Abort()
  }

  private def actorsForWorkflow(workflowId: Workflow.Id): ActorSelection =
    context.actorSelection(executionDispatcher.path./(workflowId.toString))
}
