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

package ai.deepsense.workflowexecutor

import scala.concurrent.duration.FiniteDuration

import akka.actor.{ActorContext, ActorRef}

import ai.deepsense.commons.utils.Logging
import ai.deepsense.deeplang.CommonExecutionContext
import ai.deepsense.models.workflows.Workflow

class SessionWorkflowExecutorActorProvider(
  executionContext: CommonExecutionContext,
  workflowManagerClientActor: ActorRef,
  heartbeatPublisher: ActorRef,
  notebookTopicPublisher: ActorRef,
  workflowManagerTimeout: Int,
  publisher: ActorRef,
  sessionId: String,
  heartbeatInterval: FiniteDuration
) extends Logging {
  def provide(context: ActorContext, workflowId: Workflow.Id): ActorRef = {
    context.actorOf(
      SessionWorkflowExecutorActor.props(
        executionContext,
        workflowManagerClientActor,
        publisher,
        heartbeatPublisher,
        notebookTopicPublisher,
        workflowManagerTimeout,
        sessionId,
        heartbeatInterval),
      workflowId.toString)
  }
}
