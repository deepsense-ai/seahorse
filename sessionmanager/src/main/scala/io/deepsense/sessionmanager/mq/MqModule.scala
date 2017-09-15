/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.mq

import java.util.concurrent.TimeoutException

import scala.concurrent.Future
import scala.util.control.NonFatal

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides, Singleton}
import com.rabbitmq.client.ConnectionFactory
import com.thenewmotion.akka.rabbitmq.ConnectionActor

import io.deepsense.commons.utils.Logging
import io.deepsense.sessionmanager.service.executor.SessionExecutorClients
import io.deepsense.workflowexecutor.communication.mq.MQCommunication
import io.deepsense.workflowexecutor.communication.mq.json.Global.{GlobalMQDeserializer, GlobalMQSerializer}
import io.deepsense.workflowexecutor.rabbitmq.MQCommunicationFactory

class MqModule extends AbstractModule with Logging {
  override def configure(): Unit = {}

  @Provides
  @Singleton
  def communiactionFactory(
      actorSystem: ActorSystem,
      @Named("MQConnectionActor") connection: ActorRef): MQCommunicationFactory = {
    MQCommunicationFactory(actorSystem, connection, GlobalMQSerializer, GlobalMQDeserializer)
  }

  @Provides
  @Singleton
  @Named("MQConnectionActor")
  def createConnection(
      system: ActorSystem,
      @Named("queue.host") host: String,
      @Named("queue.port") port: Int): ActorRef = {
    val factory = new ConnectionFactory()
    factory.setHost(host)
    factory.setPort(port)
    system.actorOf(
      ConnectionActor.props(factory),
      MQCommunication.mqActorSystemName)
  }

  @Provides
  @Singleton
  def createSessionExecutorClients(
      communicationFactory: MQCommunicationFactory): SessionExecutorClients = {
    new SessionExecutorClients(communicationFactory)
  }

  @Provides
  @Singleton
  @Named("SessionService.HeartbeatSubscribed")
  def heartbeatSubscriber(
      system: ActorSystem,
      communicationFactory: MQCommunicationFactory,
      @Named("SessionService.Actor") sessionServiceActor: ActorRef,
      @Named("queue.heartbeat.subscription.timeout") timeout: Long): Future[Unit] = {
    import io.deepsense.sessionmanager.mq.MQCommunicationFactoryEnrichments._
    val subscribed = communicationFactory
      .registerBroadcastSubscriber("seahorse_heartbeats_all", sessionServiceActor)

    import system.dispatcher

    val subscribedWithTimeout = Future.firstCompletedOf(List(subscribed, Future {
      Thread.sleep(timeout)
      throw new TimeoutException
    }))

    subscribedWithTimeout.onFailure {
      case NonFatal(e) =>
        logger.error(s"Haven't subscribed to Heartbeats after '$timeout' millis." +
          " Shutting down!")
        system.shutdown()
    }

    subscribedWithTimeout
  }
}
