/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.mq

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides, Singleton}
import com.rabbitmq.client.ConnectionFactory
import com.thenewmotion.akka.rabbitmq.ConnectionActor

import io.deepsense.sessionmanager.service.executor.SessionExecutorClients
import io.deepsense.workflowexecutor.communication.mq.MQCommunication
import io.deepsense.workflowexecutor.communication.mq.json.Global.{GlobalMQDeserializer, GlobalMQSerializer}
import io.deepsense.workflowexecutor.rabbitmq.MQCommunicationFactory

class MqModule extends AbstractModule {
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
}
