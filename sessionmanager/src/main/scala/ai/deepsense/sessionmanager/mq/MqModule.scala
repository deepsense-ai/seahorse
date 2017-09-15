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

import java.util.concurrent.TimeoutException

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides, Singleton}
import com.rabbitmq.client.ConnectionFactory
import com.thenewmotion.akka.rabbitmq.ConnectionActor

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

import ai.deepsense.commons.utils.Logging
import ai.deepsense.sessionmanager.service.executor.SessionExecutorClients
import ai.deepsense.sparkutils.AkkaUtils
import ai.deepsense.workflowexecutor.communication.mq.MQCommunication
import ai.deepsense.workflowexecutor.communication.mq.json.Global.{GlobalMQDeserializer, GlobalMQSerializer}
import ai.deepsense.workflowexecutor.rabbitmq.MQCommunicationFactory

class MqModule extends AbstractModule with Logging {
  override def configure(): Unit = {}

  @Provides
  @Singleton
  def communicationFactory(
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
      @Named("queue.port") port: Int,
      @Named("queue.user") user: String,
      @Named("queue.pass") pass: String): ActorRef = {
    val factory = new ConnectionFactory()
    factory.setHost(host)
    factory.setPort(port)
    factory.setUsername(user)
    factory.setPassword(pass)
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
    import ai.deepsense.sessionmanager.mq.MQCommunicationFactoryEnrichments._
    implicit val ec: ExecutionContext = system.dispatcher
    val subscribed = communicationFactory
      .registerBroadcastSubscriber("seahorse_heartbeats_all", sessionServiceActor)

    val subscribedWithTimeout = Future.firstCompletedOf(List(subscribed, Future {
      Thread.sleep(timeout)
      throw new TimeoutException
    }))

    subscribedWithTimeout.onFailure {
      case NonFatal(e) =>
        logger.error(s"Haven't subscribed to Heartbeats after '$timeout' millis." +
          " Shutting down!")
        AkkaUtils.terminate(system)
    }

    subscribedWithTimeout.map(_.data)
  }
}
