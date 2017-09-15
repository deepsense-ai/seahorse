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

import java.util.concurrent.TimeUnit

import akka.actor.{ActorPath, Actor, ActorRef, Props}
import akka.util.Timeout

import io.deepsense.commons.utils.Logging
import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowexecutor.communication._
import io.deepsense.workflowexecutor.pythongateway.PythonGateway

case class SeahorseChannelSubscriber(
  executionDispatcher: ActorRef,
  communicationFactory: MQCommunicationFactory,
  publisher: MQPublisher,
  pythonGateway: PythonGateway) extends Actor with Logging {

  implicit val timeout: Timeout = Timeout(3, TimeUnit.SECONDS)
  var publishers: Map[Workflow.Id, ActorRef] = Map()

  override def receive(): Actor.Receive = {
    case c @ ConnectMQ(workflowId) =>
      val workflowIdString = workflowId.toString
      if (!publishers.contains(workflowId)) {
        val subscriberActor =
          context.actorOf(Props(WorkflowChannelSubscriber(executionDispatcher)), workflowIdString)
        val publisher: MQPublisher =
          communicationFactory.createCommunicationChannel(workflowIdString, subscriberActor)
        val internalPublisher =
          context.actorOf(Props(new PublisherActor(publisher)), s"publishers_$workflowId")
        publishers += (workflowId -> internalPublisher)
      }
      val publisherPath: ActorPath = publishers(workflowId).path
      executionDispatcher ! WorkflowConnect(c, publisherPath)

    case get: GetPythonGatewayAddressMQ =>
      pythonGateway.listeningPort foreach { port =>
        publisher.publish(
          MQCommunication.kernelTopic,
          PythonGatewayAddress(List(SingleAddress("localhost", port))))
      }
  }
}

case class WorkflowConnect(connect: ConnectMQ, publisherPath: ActorPath)

object SeahorseChannelSubscriber {
  def props(
      executionDispatcher: ActorRef,
      communicationFactory: MQCommunicationFactory,
      publisher: MQPublisher,
      pythonGateway: PythonGateway): Props = {
    Props(new SeahorseChannelSubscriber(
      executionDispatcher, communicationFactory, publisher, pythonGateway))
  }
}
