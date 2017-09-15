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

import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout

import io.deepsense.commons.utils.Logging
import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowexecutor.communication.Connect

case class SeahorseChannelSubscriber(
  executionDispatcher: ActorRef,
  communicationFactory: MQCommunicationFactory) extends Actor with Logging {

  implicit val timeout: Timeout = Timeout(3, TimeUnit.SECONDS)
  var publishers: Map[Workflow.Id, MQPublisher] = Map()

  override def receive(): Actor.Receive = {
    case c @ Connect(workflowId) =>
      val workflowIdString = workflowId.toString
      if (!publishers.contains(workflowId)) {
        val subscriberActor =
          context.actorOf(Props(WorkflowChannelSubscriber(executionDispatcher)), workflowIdString)
        val publisher: MQPublisher =
          communicationFactory.createCommunicationChannel(workflowId.toString, subscriberActor)
        publishers += (workflowId -> publisher)
        context.actorOf(Props(new PublisherActor(publisher)), s"publishers/$workflowId")
      }
      val publisherPath = publishers(workflowId).publisherActor.path
      executionDispatcher ! c
  }
}

object SeahorseChannelSubscriber {
  def props(executionDispatcher: ActorRef, communicationFactory: MQCommunicationFactory): Props = {
    Props(new SeahorseChannelSubscriber(executionDispatcher, communicationFactory))
  }
}
