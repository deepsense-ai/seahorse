/**
 * Copyright 2016, deepsense.io
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

package io.deepsense.commons.rest.client

import java.io.FileNotFoundException

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

import akka.actor.{Actor, ActorRef, Status}

import io.deepsense.commons.models.Id

case class RetryLimitReachedExcepion(msg: String, lastError: Throwable) extends Exception(msg)

class NotebookDataPollActor(val notebookRestClient: NotebookRestClient,
    pollInterval: FiniteDuration,
    retryCountLimit: Int
) extends Actor {
  implicit val ec: ExecutionContext = context.system.dispatcher

  override def receive: Receive = {
    case NotebookDataPollActor.GetData(workflowId, nodeId) =>
      handleGetData(workflowId, nodeId, sender, 0)
    case NotebookDataPollActor.GetDataRetry(workflowId, nodeId, initialSender, retryCount) =>
      handleGetData(workflowId, nodeId, initialSender, retryCount)
  }

  def handleGetData(workflowId: Id, nodeId: Id, sender: ActorRef, retryCount: Int): Unit = {
    notebookRestClient.fetchNotebookData().onComplete {
      case Success(data) => sender ! data
      case Failure(_ : FileNotFoundException) if retryCount <= retryCountLimit =>
        context.system.scheduler.scheduleOnce(pollInterval,
          self,
          NotebookDataPollActor.GetDataRetry(workflowId, nodeId, sender, retryCount + 1))
      case Failure(f) if retryCount > retryCountLimit =>
        sender ! Status.Failure(
          RetryLimitReachedExcepion(s"Retry limit of $retryCountLimit reached, last error was $f", f))
      case Failure(f) => sender ! Status.Failure(f)
    }
  }
}

object NotebookDataPollActor {
  sealed trait Message

  case class GetData(workflowId: Id, nodeId: Id) extends Message
  case class GetDataRetry(workflowId: Id, nodeId: Id, initialSender: ActorRef, retryCount: Int) extends Message
}
