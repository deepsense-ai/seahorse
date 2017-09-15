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

package io.deepsense.workflowexecutor

import java.util.UUID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

import akka.actor.Status.Failure
import akka.actor._
import akka.pattern.{ask, pipe}

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.CommonExecutionContext
import io.deepsense.models.workflows._
import io.deepsense.workflowexecutor.WorkflowExecutorActor.Messages.Init
import io.deepsense.workflowexecutor.WorkflowManagerClientActorProtocol.GetWorkflow
import io.deepsense.workflowexecutor.communication.message.global.{Ready, ReadyContent, ReadyMessageType}
import io.deepsense.workflowexecutor.partialexecution.Execution

/**
 * Actor responsible for running workflow in an interactive way.
 */
class SessionWorkflowExecutorActor(
    executionContext: CommonExecutionContext,
    nodeExecutorFactory: GraphNodeExecutorFactory,
    workflowManagerClientActor: ActorRef,
    publisher: ActorSelection,
    seahorseTopicPublisher: ActorRef,
    wmTimeout: Int)
  extends WorkflowExecutorActor(
    executionContext,
    nodeExecutorFactory,
    Some(workflowManagerClientActor),
    Some(publisher),
    None,
    Execution.defaultExecutionFactory)
  with Logging {

  import scala.concurrent.duration._


  override def receive: Receive = {
    case Init() =>
      logger.debug("SessionWorkflowExecutorActor for: {} received INIT", workflowId.toString)
      workflowManagerClientActor.ask(GetWorkflow(workflowId))(wmTimeout seconds) pipeTo self
      context.become(waitingForWorkflow)
  }

  override def postRestart(reason: Throwable): Unit = {
    super.postRestart(reason)
    val restartId = UUID.randomUUID()
    logger.warn(
      s"SessionWorkflowExecutor actor for workflow: ${workflowId.toString} restarted: $restartId",
      reason)
    logger.info("Sending Ready message to seahorse topic after restart.")
    seahorseTopicPublisher ! Ready.afterException(reason, restartId, Some(workflowId))
  }

  def waitingForWorkflow: Actor.Receive = {
    case Some(workflowWithResults: WorkflowWithResults) =>
      logger.debug("Received workflow with id: {}", workflowId)
      context.unbecome()
      initWithWorkflow(workflowWithResults)
    case None =>
      logger.warn("Workflow with id: {} does not exist.", workflowId)
      context.unbecome()
    case Failure(e) =>
      logger.error("Could not get workflow with id", e)
      context.unbecome()
  }
}

object SessionWorkflowExecutorActor {
  def props(
    ec: CommonExecutionContext,
    workflowManagerClientActor: ActorRef,
    publisher: ActorSelection,
    seahorseTopicPublisher: ActorRef,
    wmTimeout: Int,
    statusListener: Option[ActorRef] = None): Props =
    Props(new SessionWorkflowExecutorActor(
      ec,
      new GraphNodeExecutorFactoryImpl,
      workflowManagerClientActor,
      publisher,
      seahorseTopicPublisher,
      wmTimeout))
}
