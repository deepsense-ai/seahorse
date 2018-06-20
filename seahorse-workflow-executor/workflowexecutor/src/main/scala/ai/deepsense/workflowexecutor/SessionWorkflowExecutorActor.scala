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

package ai.deepsense.workflowexecutor

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration
import scala.language.postfixOps

import akka.actor.Status.Failure
import akka.actor._
import akka.pattern.{ask, pipe}

import ai.deepsense.commons.utils.Logging
import ai.deepsense.deeplang.CommonExecutionContext
import ai.deepsense.models.workflows._
import ai.deepsense.workflowexecutor.WorkflowExecutorActor.Messages.Init
import ai.deepsense.workflowexecutor.WorkflowManagerClientActorProtocol.GetWorkflow
import ai.deepsense.workflowexecutor.communication.message.global.{Heartbeat, Ready}
import ai.deepsense.workflowexecutor.partialexecution.Execution

/**
 * Actor responsible for running workflow in an interactive way.
 */
class SessionWorkflowExecutorActor(
    executionContext: CommonExecutionContext,
    nodeExecutorFactory: GraphNodeExecutorFactory,
    workflowManagerClientActor: ActorRef,
    publisher: ActorRef,
    heartbeatPublisher: ActorRef,
    notebookPublisher: ActorRef,
    wmTimeout: Int,
    sessionId: String,
    heartbeatInterval: FiniteDuration)
  extends WorkflowExecutorActor(
    executionContext,
    nodeExecutorFactory,
    Some(workflowManagerClientActor),
    Some(publisher),
    None,
    Execution.defaultExecutionFactory)
  with Logging {

  import scala.concurrent.duration._

  private val heartbeat = Heartbeat(workflowId.toString, executionContext.sparkContext.uiWebUrl)
  private var scheduledHeartbeat: Option[Cancellable] = None

  override def receive: Receive = {
    case Init() =>
      initiate()
  }

  override def postRestart(reason: Throwable): Unit = {
    super.postRestart(reason)
    logger.warn(
      s"SessionWorkflowExecutor actor for workflow: ${workflowId.toString} " +
        "restarted. Re-initiating!",
      reason)
    initiate()
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

  override protected def onInitiated(): Unit = {
    scheduleHeartbeats()
    notebookPublisher ! Ready(sessionId)
  }

  private def scheduleHeartbeats(): Unit = {
    logger.info("Scheduling heartbeats.")
    scheduledHeartbeat.foreach(_.cancel())
    scheduledHeartbeat = Some(
      context.system.scheduler.schedule(
        Duration.Zero,
        heartbeatInterval,
        heartbeatPublisher,
        heartbeat))
  }

  private def initiate(): Unit = {
    logger.debug("SessionWorkflowExecutorActor for: {} received INIT", workflowId.toString)
    workflowManagerClientActor.ask(GetWorkflow(workflowId))(wmTimeout.seconds) pipeTo self
    context.become(waitingForWorkflow)
  }
}

object SessionWorkflowExecutorActor {
  def props(
    ec: CommonExecutionContext,
    workflowManagerClientActor: ActorRef,
    publisher: ActorRef,
    heartbeatPublisher: ActorRef,
    notebookPublisher: ActorRef,
    wmTimeout: Int,
    sessionId: String,
    heartbeatInterval: FiniteDuration): Props =
    Props(new SessionWorkflowExecutorActor(
      ec,
      new GraphNodeExecutorFactoryImpl,
      workflowManagerClientActor,
      publisher,
      heartbeatPublisher,
      notebookPublisher,
      wmTimeout,
      sessionId,
      heartbeatInterval))
}
