/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.mq

import java.util

import scala.concurrent.{ExecutionContext, Future, Promise}

import akka.actor.{ActorRef, Props}
import com.thenewmotion.akka.rabbitmq._

import io.deepsense.workflowexecutor.communication.mq.MQCommunication
import io.deepsense.workflowexecutor.rabbitmq.{ChannelSetupResult, MQCommunicationFactory, MQSubscriber}
import io.deepsense.workflowexecutor.rabbitmq.MQCommunicationFactory.NotifyingChannelActor

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
