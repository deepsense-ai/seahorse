/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.workflowexecutor.rabbitmq

import java.util
import java.util.concurrent.TimeoutException

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import com.thenewmotion.akka.rabbitmq.ChannelActor.{Connected, Disconnected}
import com.thenewmotion.akka.rabbitmq._

import ai.deepsense.commons.utils.Logging
import ai.deepsense.workflowexecutor.communication.mq.{MQCommunication, MQDeserializer, MQSerializer}
import ai.deepsense.workflowexecutor.rabbitmq.MQCommunicationFactory.NotifyingChannelActor

/**
  * A class used as a way to return data from the setupChannel callback
  *
  * @param data whatever the setupChannel returned
  * @param channelActor the channel that was just set up
  * @tparam T setupChannel return type
  */
case class ChannelSetupResult[T](data: T, channelActor: ActorRef)

case class MQCommunicationFactory(
  system: ActorSystem,
  connection: ActorRef,
  mqMessageSerializer: MQSerializer,
  mqMessageDeserializer: MQDeserializer) extends Logging {

  type QueueName = String

  val exchangeType = "topic"

  /**
    * @return future containing queue name and channel actor so we can
    *         later unsubscribe and close the channel
    */
  def registerSubscriber(topic: String, subscriber: ActorRef): Future[ChannelSetupResult[QueueName]] = {
    val channelConnected = Promise[ChannelSetupResult[QueueName]]()
    val subscriberName = MQCommunication.subscriberName(topic)
    val actorProps: Props =
      NotifyingChannelActor.props[QueueName](channelConnected, setupSubscriber(topic, subscriber))
    connection.createChannel(actorProps, Some(subscriberName))(timeout = 10.seconds)
    channelConnected.future
  }

  def deleteQueue(queue: String, channelActor: ActorRef): Unit = {
    channelActor ! ChannelMessage(_.queueDelete(queue))
    channelActor ! PoisonPill
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
    channel: Channel, self: ActorRef): QueueName = {
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
    queue
  }

  private def createMQPublisher(topic: String) = withTimeoutClue(channelTimeoutMsg){
    val publisherName = MQCommunication.publisherName(topic)
    val channelActor: ActorRef =
      connection.createChannel(ChannelActor.props(), Some(publisherName))(timeout = 10.seconds)
    MQPublisher(MQCommunication.Exchange.seahorse, mqMessageSerializer, channelActor)
  }

  private val channelTimeoutMsg =
    "Timeout while trying to connect to rabbitMQ. Make sure rabbitMQ address and port is correct."

  private def withTimeoutClue[T](clue: String)(code: => T): T = {
    try {
      code
    } catch {
      case timeout: TimeoutException =>
        logger.error(clue)
        throw timeout
    }
  }

  private  def createMQBroadcaster(exchange: String): MQPublisher = {
    val publisherName = MQCommunication.publisherName(exchange)

    def setupChannel(channel: Channel, self: ActorRef) = {
      channel.exchangeDeclare(exchange, "fanout")
    }

    val channelActor: ActorRef = connection
      .createChannel(ChannelActor.props(setupChannel), Some(publisherName))(timeout = 10.seconds)
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
  class NotifyingChannelActor[T <: AnyRef](
      channelConnected: Promise[ChannelSetupResult[T]],
      setupChannel: (Channel, ActorRef) => T) extends {
    private val setupPromise = Promise[ChannelSetupResult[T]]()
    private val setupResults: Future[ChannelSetupResult[T]] = setupPromise.future
  } with ChannelActor({
    case (channel, channelActor) =>
      // FIXME: using non-deterministic try* method as the setup may occur more than once
      setupPromise.trySuccess(ChannelSetupResult(setupChannel(channel, channelActor), channelActor))
  }) {

    onTransition {
      // FIXME: using non-deterministic try* method as the state transition may occur more than once
      case Disconnected -> Connected => channelConnected.tryCompleteWith(setupResults)
    }

  }

  object NotifyingChannelActor {
    def props[T](
        channelConnected: Promise[ChannelSetupResult[T]],
        setupChannel: (Channel, ActorRef) => T): Props = {
      Props(classOf[NotifyingChannelActor[T]], channelConnected, setupChannel)
    }
  }
}
