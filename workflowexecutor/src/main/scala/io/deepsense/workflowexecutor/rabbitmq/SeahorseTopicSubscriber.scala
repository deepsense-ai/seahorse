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

import akka.actor.{Actor, ActorPath, ActorRef, Props}

import io.deepsense.commons.utils.Logging
import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowexecutor.communication.message.global._
import io.deepsense.workflowexecutor.communication.mq.MQCommunication

case class SeahorseTopicSubscriber(
  executionDispatcher: ActorRef,
  communicationFactory: MQCommunicationFactory) extends Actor with Logging {

  var publishers: Map[Workflow.Id, ActorRef] = Map()

  override def receive(): Actor.Receive = {
    case c @ Connect(workflowId) =>
      val workflowIdString = workflowId.toString
      if (!publishers.contains(workflowId)) {
        val subscriberActor = context.actorOf(
          Props(WorkflowTopicSubscriber(workflowId, executionDispatcher.path)),
          MQCommunication.Actor.Subscriber.workflow(workflowId))
        val publisher: ActorRef = communicationFactory.createCommunicationChannel(
          workflowIdString,
          subscriberActor,
          MQCommunication.Actor.Publisher.workflow(workflowId))
        publishers += (workflowId -> publisher)
      }
      val publisherPath: ActorPath = publishers(workflowId).path
      executionDispatcher ! WorkflowConnect(c, publisherPath)
  }
}

case class WorkflowConnect(connect: Connect, publisherPath: ActorPath)

object SeahorseTopicSubscriber {
  def props(executionDispatcher: ActorRef, communicationFactory: MQCommunicationFactory): Props = {
    Props(new SeahorseTopicSubscriber(executionDispatcher, communicationFactory))
  }
}
