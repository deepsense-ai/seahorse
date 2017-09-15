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

import scala.concurrent.{Future, Promise}

import akka.actor.{ActorRef, ActorSystem, Props}
import com.rabbitmq.client.Channel
import com.thenewmotion.akka.rabbitmq.ChannelActor.{Connected, Disconnected}
import com.thenewmotion.akka.rabbitmq._

import io.deepsense.workflowexecutor.communication.mq.MQCommunication
import io.deepsense.workflowexecutor.communication.mq.serialization.{MessageMQDeserializer, MessageMQSerializer}
import io.deepsense.workflowexecutor.rabbitmq.MQCommunicationFactory.NotifyingChannelActor

case class MQCommunicationFactory(
  system: ActorSystem,
  connection: ActorRef,
  mqMessageSerializer: MessageMQSerializer,
  mqMessageDeserializer: MessageMQDeserializer) {

  val exchangeType = "topic"

  def registerSubscriber(topic: String, subscriber: ActorRef): Future[Unit] = {
    val channelConnected = Promise[Unit]()
    val subscriberName = MQCommunication.subscriberName(topic)
    val actorProps: Props =
      NotifyingChannelActor.props(channelConnected, setupSubscriber(topic, subscriber))
    connection.createChannel(actorProps, Some(subscriberName))
    channelConnected.future
  }

  def createPublisher(topic: String, publisherActorName: String): ActorRef = {
    val publisher = createMQPublisher(topic)
    system.actorOf(PublisherActor.props(topic, publisher), publisherActorName)
  }

  def createBroadcaster(exchange: String, publishingActorName: String): ActorRef = {
    val publisher = createMQBroadcaster(exchange)
    system.actorOf(PublisherActor.props(exchange, publisher), publishingActorName)
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

  private  def createMQBroadcaster(exchange: String): MQPublisher = {
    val publisherName = MQCommunication.publisherName(exchange)

    def setupChannel(channel: Channel, self: ActorRef) = {
      channel.exchangeDeclare(exchange, "fanout")
    }

    val channelActor: ActorRef =
      connection.createChannel(ChannelActor.props(setupChannel), Some(publisherName))
    MQPublisher(exchange, mqMessageSerializer, channelActor)
  }
}

object MQCommunicationFactory {

  /**
   * NotifyingChannelActor extends ChannelActor behaviour by completing a promise after connecting
   * to a channel.
   * In the thenewmotion's library for RMQ, it's impossible to synchronously connect to a channel.
   * NotifyingChannelActor resolves this issue by allowing to wait for the promise to complete.
   *
   * <br><br>
   * More on the issue:<br>
   * As the documentation says, to synchronously create a channel one have to call createChannel
   * method.
   * {{{
   *   import com.thenewmotion.akka.rabbitmq.reachConnectionActor
   *   val channelActor: ActorRef = connectionActor.createChannel(ChannelActor.props())
   * }}}
   *
   * It's true that the method awaits channel creation. The code looks like the one below:
   * {{{
   *   val future = self ? CreateChannel(props, name)
   *   Await.result(future, timeout.duration).asInstanceOf[ChannelCreated].channel
   * }}}
   *
   * However, ConnectionActor's CreateChannel handler is asynchronous:
   * {{{
   *   val child = newChild(props, name)
   *   log.debug("{} creating child {} with channel {}", header(Connected, msg), child, channel)
   *   child ! channel
   *   stay replying ChannelCreated(child)
   * }}}
   *
   * Thus, it is possible that the handler responds ChannelCreated before the channel is Created.
   */
  class NotifyingChannelActor(
      channelConnected: Promise[Unit],
      setupChannel: (Channel, ActorRef) => Any)
    extends ChannelActor(setupChannel) {
    onTransition {
      case Disconnected -> Connected => channelConnected.trySuccess(Unit)
    }
  }

  object NotifyingChannelActor {
    def props(
        channelConnected: Promise[Unit],
        setupChannel: (Channel, ActorRef) => Any = (_, _) => ()): Props = {
      Props(classOf[NotifyingChannelActor], channelConnected, setupChannel)
    }
  }
}
