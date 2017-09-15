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
import io.deepsense.workflowexecutor.communication.message.notebook.{Address, GetPythonGatewayAddress, PythonGatewayAddress}

case class NotebookKernelTopicSubscriber(
    publisherActorName: String,
    pythonGatewayListeningPort: () => Option[Int],
    localAddress: String)
  extends Actor
  with Logging {

  override def receive(): Actor.Receive = {
    case get: GetPythonGatewayAddress =>
      logger.debug("Received GetPythonGatewayAddress")
      pythonGatewayListeningPort() foreach { port =>
        publisherActor(publisherActorName) ! PythonGatewayAddress(List(Address(localAddress, port)))
      }
  }

  private[this] def publisherActor(actorName: String): ActorSelection = {
    context.system.actorSelection(actorName)
  }
}

object NotebookKernelTopicSubscriber {
  def props(
      publisherActorName: String,
      pythonGatewayListeningPort: () => Option[Int],
      localAddress: String): Props = {
    Props(new NotebookKernelTopicSubscriber(
      publisherActorName,
      pythonGatewayListeningPort,
      localAddress))
  }
}
