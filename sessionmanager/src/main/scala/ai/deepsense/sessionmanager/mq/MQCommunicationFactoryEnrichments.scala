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

package ai.deepsense.sessionmanager.mq

import java.util

import scala.concurrent.{ExecutionContext, Future, Promise}

import akka.actor.{ActorRef, Props}
import com.thenewmotion.akka.rabbitmq._

import ai.deepsense.workflowexecutor.communication.mq.MQCommunication
import ai.deepsense.workflowexecutor.rabbitmq.{ChannelSetupResult, MQCommunicationFactory, MQSubscriber}
import ai.deepsense.workflowexecutor.rabbitmq.MQCommunicationFactory.NotifyingChannelActor

object MQCommunicationFactoryEnrichments {

  implicit class RichMQCommunicationFactory(factory: MQCommunicationFactory) {

    def registerBroadcastSubscriber(
        exchange: String,
        subscriber: ActorRef): Future[ChannelSetupResult[Unit]] = {
      val channelConnected = Promise[ChannelSetupResult[Unit]]()
      val subscriberName = MQCommunication.subscriberName(exchange)
      val actorProps: Props = NotifyingChannelActor
        .props(channelConnected, setupBroadcastSubscriber(exchange, subscriber))
      factory.connection.createChannel(actorProps, Some(subscriberName))
      channelConnected.future
    }

    private def setupBroadcastSubscriber(
        exchange: String,
        subscriber: ActorRef)(
        channel: Channel, self: ActorRef): Unit = {
      channel.exchangeDeclare(exchange, "fanout")
      val queueName: String = MQCommunication.queueName(exchange)
      val queue = channel.queueDeclare(
        queueName,
        false,
        false,
        true,
        new util.HashMap[String, AnyRef]()).getQueue
      channel.queueBind(queue, exchange, queueName)
      val consumer = MQSubscriber(subscriber, factory.mqMessageDeserializer, channel)
      channel.basicConsume(queue, true, consumer)
    }
  }
}
