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

package io.deepsense.workflowexecutor

import akka.actor.{ActorSystem, Props}
import com.rabbitmq.client.ConnectionFactory
import com.thenewmotion.akka.rabbitmq.ConnectionActor

import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowexecutor.communication.{MQCommunication, Connect, ProtocolDeserializer}
import io.deepsense.workflowexecutor.rabbitmq.{MQPublisher, MQCommunicationFactory, MySubscriber, SubscriberActor}

object SessionExecutorApp {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    val factory = new ConnectionFactory()
    val connection = system.actorOf(
      ConnectionActor.props(factory),
      MQCommunication.mqActorSystemName)
    val exchange = "seahorse"

    val mySubscriber = system.actorOf(Props[MySubscriber], "mySubscriber")
    val communicationFactory = MQCommunicationFactory(system, connection)

    val globalMessageDeserializer = ProtocolDeserializer()
    val globalSubscriber = SubscriberActor(mySubscriber, globalMessageDeserializer)

    val globalPublisher: MQPublisher = communicationFactory.createCommunicationChannel(
      exchange, globalSubscriber)


    while (true) {
      globalPublisher.publish("to_executor", Connect(Workflow.Id.randomId))
      Thread.sleep(5000)
    }
  }
}
