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

import java.util

import akka.actor.{ActorRef, ActorSystem}
import com.rabbitmq.client.Channel
import com.thenewmotion.akka.rabbitmq.{ChannelActor, CreateChannel, RichConnectionActor}

import io.deepsense.workflowexecutor.communication.mq.MQCommunication
import io.deepsense.workflowexecutor.communication.mq.serialization.{MessageMQDeserializer, MessageMQSerializer}

case class MQCommunicationFactory(
  system: ActorSystem,
  connection: ActorRef,
  mqMessageSerializer: MessageMQSerializer,
  mqMessageDeserializer: MessageMQDeserializer) {

  val exchangeType = "topic"

  def registerSubscriber(topic: String, subscriber: ActorRef): Unit = {
    val subscriberName = MQCommunication.subscriberName(topic)
    connection ! CreateChannel(
      ChannelActor.props(setupSubscriber(topic, subscriber)),
      Some(subscriberName))
  }

  def createPublisher(topic: String, publisherActorName: String): ActorRef = {
    val publisher = createMQPublisher(topic)
    system.actorOf(PublisherActor.props(topic, publisher), publisherActorName)
  }

  private def setupSubscriber(
    topic: String,
    subscriber: ActorRef)(
    channel: Channel, self: ActorRef): Unit = {
    val queueName: String = MQCommunication.queueName(topic)
    val queue = channel.queueDeclare(
      queueName,
      false,
      false,
      true,
      new util.HashMap[String, AnyRef]()).getQueue
    channel.queueBind(queue, MQCommunication.Exchange.seahorse, topic)
    val consumer = MQSubscriber(subscriber, mqMessageDeserializer, channel)
    channel.basicConsume(queue, true, consumer)
  }

  private def createMQPublisher(topic: String): MQPublisher = {
    val publisherName = MQCommunication.publisherName(topic)
    val channelActor: ActorRef =
      connection.createChannel(ChannelActor.props(), Some(publisherName))
    MQPublisher(MQCommunication.Exchange.seahorse, mqMessageSerializer, channelActor)
  }
}
