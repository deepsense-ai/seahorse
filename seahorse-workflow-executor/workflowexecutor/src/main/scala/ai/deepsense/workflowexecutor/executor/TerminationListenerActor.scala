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

package ai.deepsense.workflowexecutor.executor

import scala.concurrent.Promise

import akka.actor.{Actor, Props}

import ai.deepsense.sparkutils.AkkaUtils
import ai.deepsense.models.workflows.ExecutionReport

class TerminationListenerActor(finishedExecutionStatus: Promise[ExecutionReport]) extends Actor {
  override def receive: Receive = {
    case status: ExecutionReport =>
      finishedExecutionStatus.success(status)
      AkkaUtils.terminate(context.system)
  }
}

object TerminationListenerActor {
  def props(finishedExecutionReport: Promise[ExecutionReport]): Props =
    Props(new TerminationListenerActor(finishedExecutionReport))
}
