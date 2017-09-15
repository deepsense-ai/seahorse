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

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.routing._
import com.rabbitmq.client.ConnectionFactory
import com.thenewmotion.akka.rabbitmq._
import com.typesafe.config.ConfigFactory
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowexecutor.WorkflowExecutorActor.Messages.Init
import io.deepsense.workflowexecutor.communication.mq.MQCommunication
import io.deepsense.workflowexecutor.communication.mq.serialization.json.{ProtocolJsonDeserializer, ProtocolJsonSerializer}
import io.deepsense.workflowexecutor.executor.session.LivyKeepAliveActor
import io.deepsense.workflowexecutor.notebooks.KernelManagerCaretaker
import io.deepsense.workflowexecutor.pyspark.PythonPathGenerator
import io.deepsense.workflowexecutor.rabbitmq._
import io.deepsense.workflowexecutor.session.storage.DataFrameStorageImpl
import io.deepsense.workflowexecutor._

/**
 * SessionExecutor waits for user instructions in an infinite loop.
 */
case class SessionExecutor(
    messageQueueHost: String,
    messageQueuePort: Int,
    sessionId: String,
    wmAddress: String,
    depsZip: String)
  extends Executor {

  private val workflowId = Workflow.Id.fromString(sessionId)
  private val config = ConfigFactory.load
  private val subscriptionTimeout = config.getInt("subscription-timeout").seconds
  private val keepAliveInterval = config.getInt("keep-alive.interval").seconds
  private val heartbeatInterval = config.getInt("heartbeat.interval").seconds
  private val workflowManagerTimeout = config.getInt("workflow-manager.timeout")
  private val wmWorkflowsPath = config.getString("workflow-manager.workflows.path")
  private val wmReportsPath = config.getString("workflow-manager.reports.path")

  val graphReader = new GraphReader(createDOperationsCatalog())

  /**
   * WARNING: Performs an infinite loop.
   */
  def execute(): Unit = {
    logger.info(s"SessionExecutor for '$workflowId' starts...")
    val sparkContext = createSparkContext()
    val sqlContext = createSqlContext(sparkContext)
    val dOperableCatalog = createDOperableCatalog()
    val dataFrameStorage = new DataFrameStorageImpl

    val hostAddress: InetAddress = HostAddressResolver.findHostAddress()
    logger.info("Host address: {}", hostAddress.getHostAddress)

    val tempPath = Unzip.unzipAll(depsZip)

    val pythonPathGenerator = new pyspark.Loader(Some(tempPath)).load
      .map(new PythonPathGenerator(_))
      .getOrElse(throw new RuntimeException("Could not find PySpark!"))

    val pythonExecutionCaretaker = new PythonExecutionCaretaker(
      s"$tempPath/pyexecutor/pyexecutor.py",
      pythonPathGenerator,
      sparkContext,
      sqlContext,
      dataFrameStorage,
      hostAddress)
    pythonExecutionCaretaker.start()

    implicit val system = ActorSystem()
    setupLivyKeepAliveLogging(system, keepAliveInterval)
    val workflowManagerClientActor = system.actorOf(
      WorkflowManagerClientActor.props(
        wmAddress,
        wmWorkflowsPath,
        wmReportsPath,
        graphReader))

    val communicationFactory: MQCommunicationFactory = createCommunicationFactory(system)

    val workflowsSubscriberActor: ActorRef = createWorkflowsSubscriberActor(
      sparkContext,
      sqlContext,
      dOperableCatalog,
      dataFrameStorage,
      pythonExecutionCaretaker,
      system,
      workflowManagerClientActor,
      communicationFactory)

    val workflowsSubscriberReady = communicationFactory.registerSubscriber(
      MQCommunication.Topic.allWorkflowsSubscriptionTopic(sessionId),
      workflowsSubscriberActor)

    waitUntilSubscribersAreReady(Seq(workflowsSubscriberReady))

    val kernelManagerCaretaker = new KernelManagerCaretaker(
      system,
      pythonPathGenerator,
      communicationFactory,
      tempPath,
      hostAddress.getHostAddress,
      pythonExecutionCaretaker.gatewayListeningPort.get,
      messageQueueHost,
      messageQueuePort,
      sessionId,
      workflowId
    )

    kernelManagerCaretaker.start()

    logger.info(s"Sending Init() to WorkflowsSubscriberActor")
    workflowsSubscriberActor ! Init()

    system.awaitTermination()
    cleanup(system, sparkContext, pythonExecutionCaretaker, kernelManagerCaretaker)
    logger.debug("SessionExecutor ends")
  }

  private def createWorkflowsSubscriberActor(
      sparkContext: SparkContext,
      sqlContext: SQLContext,
      dOperableCatalog: DOperableCatalog,
      dataFrameStorage: DataFrameStorageImpl,
      pythonExecutionCaretaker: PythonExecutionCaretaker,
      system: ActorSystem,
      workflowManagerClientActor: ActorRef,
      communicationFactory: MQCommunicationFactory): ActorRef = {

    def createHeartbeatPublisher: ActorRef = {
      val seahorsePublisher = communicationFactory.createPublisher(
        MQCommunication.Topic.seahorsePublicationTopic(sessionId),
        MQCommunication.Actor.Publisher.seahorse)

      val heartbeatBroadcaster = communicationFactory.createBroadcaster(
        MQCommunication.Exchange.heartbeats(workflowId),
        MQCommunication.Actor.Publisher.heartbeat(workflowId)
      )

      val routeePaths = scala.collection.immutable.Iterable(seahorsePublisher, heartbeatBroadcaster)
        .map(_.path.toString)

      val heartbeatPublisher = system.actorOf(
        Props.empty.withRouter(BroadcastGroup(routeePaths)),
        "heartbeatBroadcastingRouter"
      )
      heartbeatPublisher
    }

    val heartbeatPublisher: ActorRef = createHeartbeatPublisher

    val executionContext = createExecutionContext(
      dataFrameStorage,
      pythonExecutionCaretaker,
      sparkContext,
      sqlContext,
      dOperableCatalog = Some(dOperableCatalog))

    val readyBroadcaster = communicationFactory.createBroadcaster(
      MQCommunication.Exchange.ready(workflowId),
      MQCommunication.Actor.Publisher.ready(workflowId))

    val publisher: ActorRef = communicationFactory.createPublisher(
      MQCommunication.Topic.workflowPublicationTopic(workflowId, sessionId),
      MQCommunication.Actor.Publisher.workflow(workflowId))

    val actorProvider = new SessionWorkflowExecutorActorProvider(
      executionContext,
      workflowManagerClientActor,
      heartbeatPublisher,
      readyBroadcaster,
      workflowManagerTimeout,
      publisher,
      sessionId,
      heartbeatInterval)

    val workflowsSubscriberActor = system.actorOf(
      WorkflowTopicSubscriber.props(
        actorProvider,
        sessionId,
        workflowId),
      MQCommunication.Actor.Subscriber.workflows)

    workflowsSubscriberActor
  }

  private def createCommunicationFactory(system: ActorSystem): MQCommunicationFactory = {
    val connection: ActorRef = createConnection(system)
    val messageDeserializer = ProtocolJsonDeserializer(graphReader)
    val messageSerializer = ProtocolJsonSerializer(graphReader)
    MQCommunicationFactory(system, connection, messageSerializer, messageDeserializer)
  }

  private def createConnection(system: ActorSystem): ActorRef = {
    val factory = new ConnectionFactory()
    factory.setHost(messageQueueHost)
    factory.setPort(messageQueuePort)
    system.actorOf(
      ConnectionActor.props(factory),
      MQCommunication.mqActorSystemName)
  }

  // Clients after receiving ready or heartbeat will assume
  // that we are listening for their response
  private def waitUntilSubscribersAreReady(subscribers: Seq[Future[Unit]]): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val subscribed: Future[Seq[Unit]] = Future.sequence(subscribers)
    logger.info("Waiting for subscribers...")
    Await.result(subscribed, subscriptionTimeout)
    logger.info("Subscribers READY!")
  }

  private def setupLivyKeepAliveLogging(system: ActorSystem, interval: FiniteDuration): Unit =
    system.actorOf(LivyKeepAliveActor.props(interval), "KeepAliveActor")

  private def cleanup(
      system: ActorSystem,
      sparkContext: SparkContext,
      pythonExecutionCaretaker: PythonExecutionCaretaker,
      kernelManagerCaretaker: KernelManagerCaretaker): Unit = {
    logger.debug("Cleaning up...")
    pythonExecutionCaretaker.stop()
    kernelManagerCaretaker.stop()
    sparkContext.stop()
    logger.debug("Spark terminated!")
    system.shutdown()
    logger.debug("Akka terminated!")
  }
}
