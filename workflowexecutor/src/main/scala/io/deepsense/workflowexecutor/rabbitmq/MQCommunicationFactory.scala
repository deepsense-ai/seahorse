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

import io.deepsense.workflowexecutor.communication.{MQCommunication, MQMessageDeserializer}

case class MQCommunicationFactory(
  system: ActorSystem,
  connection: ActorRef,
  mQMessageDeserializer: MQMessageDeserializer) {

  val exchangeType = "direct"

  def createCommunicationChannel(name: String, subscriber: ActorRef): MQPublisher = {
    createSubscriber(name, SubscriberActor(subscriber, mQMessageDeserializer))
    createPublisher(name)
  }

  def createCommunicationChannel(name: String, subscriber: MQPublisher => ActorRef): Unit = {
    val publisher = createPublisher(name)
    createSubscriber(name, SubscriberActor(subscriber(publisher), mQMessageDeserializer))
  }

  private def createSubscriber(exchangeName: String, subscriber: SubscriberActor): Unit = {
    val subscriberName = s"${exchangeName}_subscriber"
    connection ! CreateChannel(
      ChannelActor.props(setupSubscriber(exchangeName, subscriber)),
      Some(subscriberName))
  }

  private def setupSubscriber(
    exchangeName: String,
    subscriberActor: SubscriberActor)(
    channel: Channel, self: ActorRef): Unit = {
    val queue = channel.queueDeclare(
      s"${exchangeName}_to_executor",
      false,
      false,
      true,
      new util.HashMap[String, AnyRef]()).getQueue
    declareExchange(exchangeName, channel)
    channel.queueBind(queue, exchangeName, "to_executor")
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

  private def createPublisher(exchangeName: String): MQPublisher = {
    val publisherName = s"${exchangeName}_publisher"
  val channelActor: ActorRef =
    connection.createChannel(ChannelActor.props(setupPublisher(exchangeName)), Some(publisherName))
    MQPublisher(exchangeName, channelActor)
  }

  private def setupPublisher(exchangeName: String)(channel: Channel, self: ActorRef): Unit =
    declareExchange(exchangeName, channel)

  private def declareExchange(exchangeName: String, channel: Channel) = {
    channel.exchangeDeclare(exchangeName, exchangeType)
  }
}
