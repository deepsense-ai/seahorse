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

package io.deepsense.workflowexecutor.executor

import java.net.InetAddress
import java.util.concurrent.TimeUnit

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

import akka.actor.{ActorRef, ActorSystem, Props}
import com.rabbitmq.client.ConnectionFactory
import com.thenewmotion.akka.rabbitmq._
import com.typesafe.config.ConfigFactory
import org.apache.spark.SparkContext

import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.workflowexecutor.communication.message.global.{Heartbeat, Ready}
import io.deepsense.workflowexecutor.communication.mq.MQCommunication
import io.deepsense.workflowexecutor.communication.mq.serialization.json.{ProtocolJsonDeserializer, ProtocolJsonSerializer}
import io.deepsense.workflowexecutor.executor.session.LivyKeepAliveActor
import io.deepsense.workflowexecutor.rabbitmq._
import io.deepsense.workflowexecutor.session.storage.DataFrameStorageImpl
import io.deepsense.workflowexecutor.{ExecutionDispatcherActor, StatusLoggingActor, WorkflowManagerClientActor}

/**
 * SessionExecutor waits for user instructions in an infinite loop.
 */
case class SessionExecutor(
    messageQueueHost: String,
    pythonExecutorPath: String,
    sessionId: String)
  extends Executor {

  private val config = ConfigFactory.load
  private val subscriptionTimeout =
    Duration(config.getInt("subscription-timeout"), TimeUnit.SECONDS)
  private val keepAliveInterval =
    FiniteDuration(config.getInt("keep-alive.interval"), TimeUnit.SECONDS)
  private val heartbeatInterval =
    FiniteDuration(config.getInt("heartbeat.interval"), TimeUnit.SECONDS)
  val graphReader = new GraphReader(createDOperationsCatalog())

  /**
   * WARNING: Performs an infinite loop.
   */
  def execute(): Unit = {
    logger.debug("SessionExecutor starts")
    val sparkContext = createSparkContext()
    val sqlContext = createSqlContext(sparkContext)
    val dOperableCatalog = createDOperableCatalog()
    val dataFrameStorage = new DataFrameStorageImpl

    val hostAddress: InetAddress = HostAddressResolver.findHostAddress()
    logger.info("HOST ADDRESS: {}", hostAddress.getHostAddress)

    val pythonExecutionCaretaker = new PythonExecutionCaretaker(
      pythonExecutorPath,
      sparkContext,
      sqlContext,
      dataFrameStorage,
      hostAddress)
    pythonExecutionCaretaker.start()

    implicit val system = ActorSystem()
    val statusLogger = system.actorOf(Props[StatusLoggingActor], "status-logger")
    val workflowManagerClientActor = system.actorOf(
      WorkflowManagerClientActor.props(
        config.getString("workflow-manager.local.address"),
        config.getString("workflow-manager.workflows.path"),
        config.getString("workflow-manager.reports.path"),
        graphReader))

    val factory = new ConnectionFactory()
    factory.setHost(messageQueueHost)

    val connection = system.actorOf(
      ConnectionActor.props(factory),
      MQCommunication.mqActorSystemName)

    val messageDeserializer = ProtocolJsonDeserializer(graphReader)
    val messageSerializer = ProtocolJsonSerializer(graphReader)
    val communicationFactory =
      MQCommunicationFactory(system, connection, messageSerializer, messageDeserializer)

    val seahorsePublisher = communicationFactory.createPublisher(
      MQCommunication.Topic.seahorsePublicationTopic(sessionId),
      MQCommunication.Actor.Publisher.seahorse)
    val notebookPublisher = communicationFactory.createPublisher(
      MQCommunication.Topic.notebookPublicationTopic,
      MQCommunication.Actor.Publisher.notebook)

    val executionDispatcher = system.actorOf(ExecutionDispatcherActor.props(
      sparkContext,
      sqlContext,
      dOperableCatalog,
      dataFrameStorage,
      pythonExecutionCaretaker,
      statusLogger,
      workflowManagerClientActor,
      seahorsePublisher,
      notebookPublisher,
      config.getInt("workflow-manager.timeout")), "workflows")

    val notebookSubscriberActor = system.actorOf(
      NotebookKernelTopicSubscriber.props(
        "user/" + MQCommunication.Actor.Publisher.notebook,
        pythonExecutionCaretaker.gatewayListeningPort _,
        hostAddress.getHostAddress),
      MQCommunication.Actor.Subscriber.notebook)
    val notebookSubscriberReady = communicationFactory.registerSubscriber(
      MQCommunication.Topic.notebookSubscriptionTopic,
      notebookSubscriberActor)

    val workflowsSubscriberActor = system.actorOf(
      WorkflowTopicSubscriber.props(executionDispatcher, communicationFactory, sessionId),
      MQCommunication.Actor.Subscriber.workflows)
    val workflowsSubscriberReady = communicationFactory.registerSubscriber(
      MQCommunication.Topic.allWorkflowsSubscriptionTopic(sessionId),
      workflowsSubscriberActor)

    waitUntilSubscribersAreReady(Seq(notebookSubscriberReady, workflowsSubscriberReady))

    sendReady(Seq(notebookPublisher, seahorsePublisher))
    setupHeartbeat(system, seahorsePublisher)
    setupLivyKeepAliveLogging(system, keepAliveInterval)

    system.awaitTermination()
    cleanup(sparkContext, pythonExecutionCaretaker)
    logger.debug("SessionExecutor ends")
  }

  // Clients after receiving ready or heartbeat will assume
  // that we are listening for their response
  private def waitUntilSubscribersAreReady(subscribers: Seq[Future[Unit]]): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val subscribed: Future[Seq[Unit]] = Future.sequence(subscribers)
    logger.info("Waiting for subscribers...")
    Await.result(subscribed, subscriptionTimeout)
  }

  private def sendReady(publishers: Seq[ActorRef]): Unit =
    publishers.foreach(_ ! Ready.seahorseIsReady)

  private def setupHeartbeat(system: ActorSystem, seahorsePublisher: ActorRef): Unit = {
    val heartbeat = Heartbeat(sessionId)
    import scala.concurrent.ExecutionContext.Implicits.global
    system.scheduler.schedule(Duration.Zero, heartbeatInterval, seahorsePublisher, heartbeat)
  }

  private def setupLivyKeepAliveLogging(system: ActorSystem, interval: FiniteDuration): Unit =
    system.actorOf(LivyKeepAliveActor.props(interval), "KeepAliveActor")

  private def cleanup(
      sparkContext: SparkContext,
      pythonExecutionCaretaker: PythonExecutionCaretaker): Unit = {
    logger.debug("Cleaning up...")
    pythonExecutionCaretaker.stop()
    sparkContext.stop()
    logger.debug("Spark terminated!")
  }

}
