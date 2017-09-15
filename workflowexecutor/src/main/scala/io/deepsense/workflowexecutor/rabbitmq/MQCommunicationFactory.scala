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
import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.{Channel, DefaultConsumer, Envelope}
import com.thenewmotion.akka.rabbitmq.{ChannelActor, CreateChannel, RichConnectionActor}

import io.deepsense.workflowexecutor.communication.mq.MQCommunication
import io.deepsense.workflowexecutor.communication.mq.serialization.{MessageMQDeserializer, MessageMQSerializer}

case class MQCommunicationFactory(
  system: ActorSystem,
  connection: ActorRef,
  mqMessageSerializer: MessageMQSerializer,
  mqMessageDeserializer: MessageMQDeserializer) {

  val exchangeType = "topic"

  def createCommunicationChannel(
      topic: String,
      subscriber: ActorRef,
      publisherActorName: String): ActorRef = {
    createSubscriber(topic, SubscriberActor(subscriber, mqMessageDeserializer))
    val publisher = createPublisher(topic)
    system.actorOf(
      PublisherActor.props(topic, publisher),
      publisherActorName)

  }

  private def createSubscriber(topic: String, subscriber: SubscriberActor): Unit = {
    val subscriberName = MQCommunication.subscriberName(topic)
    connection ! CreateChannel(
      ChannelActor.props(setupSubscriber(topic, subscriber)),
      Some(subscriberName))
  }

  private def setupSubscriber(
    topic: String,
    subscriberActor: SubscriberActor)(
    channel: Channel, self: ActorRef): Unit = {
    val queueName: String = MQCommunication.queueName(topic)
    val queue = channel.queueDeclare(
      queueName,
      false,
      false,
      true,
      new util.HashMap[String, AnyRef]()).getQueue
    declareExchange(channel)
    channel.queueBind(queue, MQCommunication.Exchange.seahorse, topic)
    val basicSubscriber = MQSubscriber(subscriberActor)
    val consumer = new DefaultConsumer(channel) {
      override def handleDelivery(
        consumerTag: String,
        envelope: Envelope,
        properties: BasicProperties,
        body: Array[Byte]): Unit =
        basicSubscriber.handleDelivery(body)
    }
    channel.basicConsume(queue, true, consumer)
  }

  private def createPublisher(topic: String): MQPublisher = {
    val publisherName = MQCommunication.publisherName(topic)
    val channelActor: ActorRef =
      connection.createChannel(ChannelActor.props(setupPublisher()), Some(publisherName))
    MQPublisher(topic, mqMessageSerializer, channelActor)
  }

  private def setupPublisher()(channel: Channel, self: ActorRef): Unit =
    declareExchange(channel)

  private def declareExchange(channel: Channel) = {
    channel.exchangeDeclare(MQCommunication.Exchange.seahorse, exchangeType)
  }
}
