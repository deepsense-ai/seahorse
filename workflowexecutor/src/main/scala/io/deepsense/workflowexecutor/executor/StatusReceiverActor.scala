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

package io.deepsense.workflowexecutor.executor

import scala.concurrent.Promise

import akka.actor.{Actor, Props}

import io.deepsense.graph.graphstate._
import io.deepsense.workflowexecutor.communication.ExecutionStatus

class StatusReceiverActor(finishedExecutionStatus: Promise[ExecutionStatus]) extends Actor {
  override def receive: Receive = {
    case status @ ExecutionStatus(graphState, _, _) =>
      graphState match {
        case Draft | Running => // The graph is still running
        case Completed | Aborted | Failed(_) =>
          finishedExecutionStatus.success(status)
          context.system.shutdown()
      }
  }
}

object StatusReceiverActor {
  def props(finishedExecutionStatus: Promise[ExecutionStatus]): Props =
    Props(new StatusReceiverActor(finishedExecutionStatus))
}
