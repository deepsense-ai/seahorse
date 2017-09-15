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

import akka.actor.{ActorRef, Props, Actor}
import io.deepsense.commons.utils.Logging
import io.deepsense.workflowexecutor.{WorkflowExecutorActor, ExecutionDispatcherActor}
import io.deepsense.workflowexecutor.communication.{Launch, Connect}

class MySubscriber(executionDispatcher: ActorRef) extends Actor with Logging {

  override def receive(): Actor.Receive = {
    // scalastyle:off println
    case x @ Connect(workflowId) =>
      println(s"odebralem connect, workflowId: $workflowId")
      executionDispatcher ! x
    // scalastyle:on println
    case Launch(workflow) =>
//      println(s"odebralem connect, workflowId")
      // TODO variables from main
      logger.debug(s"LAUNCH! $workflow")
      val selection = context
        .actorSelection(executionDispatcher.path./(workflow.id.toString))
      selection ! WorkflowExecutorActor.Messages.Launch(workflow.graph)
  }
}

object MySubscriber {
  def props(executionDispatcher: ActorRef): Props = {
    Props(new MySubscriber(executionDispatcher))
  }
}
