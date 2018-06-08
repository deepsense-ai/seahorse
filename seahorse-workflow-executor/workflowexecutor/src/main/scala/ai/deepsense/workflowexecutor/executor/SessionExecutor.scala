/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.workflowexecutor.executor

import java.net.{InetAddress, URL}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.routing._
import com.rabbitmq.client.ConnectionFactory
import com.thenewmotion.akka.rabbitmq._
import com.typesafe.config.ConfigFactory
import org.apache.spark.SparkContext

import ai.deepsense.commons.mail.{EmailSender, EmailSenderAuthorizationConfig, EmailSenderConfig}
import ai.deepsense.deeplang._
import ai.deepsense.commons.rest.client.NotebooksClientFactory
import ai.deepsense.commons.rest.client.datasources.DatasourceRestClientFactory
import ai.deepsense.deeplang.catalogs.DCatalog
import ai.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import ai.deepsense.models.workflows.Workflow
import ai.deepsense.sparkutils.{AkkaUtils, SparkSQLSession}
import ai.deepsense.workflowexecutor.WorkflowExecutorActor.Messages.Init
import ai.deepsense.workflowexecutor.communication.mq.MQCommunication
import ai.deepsense.workflowexecutor.communication.mq.json.Global.{GlobalMQDeserializer, GlobalMQSerializer}
import ai.deepsense.workflowexecutor.communication.mq.serialization.json.{ProtocolJsonDeserializer, ProtocolJsonSerializer}
import ai.deepsense.workflowexecutor.customcode.CustomCodeEntryPoint
import ai.deepsense.workflowexecutor.executor.session.LivyKeepAliveActor
import ai.deepsense.workflowexecutor.notebooks.KernelManagerCaretaker
import ai.deepsense.workflowexecutor.pyspark.PythonPathGenerator
import ai.deepsense.workflowexecutor.rabbitmq._
import ai.deepsense.workflowexecutor.session.storage.DataFrameStorageImpl
import ai.deepsense.workflowexecutor.{WorkflowManagerClientActor, _}

/**
 * SessionExecutor waits for user instructions in an infinite loop.
 */
case class SessionExecutor(
    messageQueueHost: String,
    messageQueuePort: Int,
    messageQueueUser: String,
    messageQueuePass: String,
    workflowId: String,
    wmAddress: String,
    wmUsername: String,
    wmPassword: String,
    mailServerHost: String,
    mailServerPort: Int,
    mailServerUser: String,
    mailServerPassword: String,
    mailServerSender: String,
    notebookServerAddress: URL,
    datasourceServerAddress: URL,
    depsZip: String,
    workflowOwnerId: String,
    tempPath: String,
    pythonBinaryPath: Option[String])
  extends Executor {

  private val workflowIdObject = Workflow.Id.fromString(workflowId)
  private val config = ConfigFactory.load
  private val subscriptionTimeout = config.getInt("subscription-timeout").seconds
  private val keepAliveInterval = config.getInt("keep-alive.interval").seconds
  private val heartbeatInterval = config.getInt("heartbeat.interval").seconds
  private val workflowManagerTimeout = config.getInt("workflow-manager.timeout")
  private val wmWorkflowsPath = config.getString("workflow-manager.workflows.path")
  private val wmReportsPath = config.getString("workflow-manager.reports.path")
  val DCatalog(_, dOperableCatalog, dOperationsCatalog) =
    CatalogRecorder.resourcesCatalogRecorder.catalogs

  val graphReader = new GraphReader(dOperationsCatalog)

  /**
   * WARNING: Performs an infinite loop.
   */
  def execute(): Unit = {
    logger.info(s"SessionExecutor for '$workflowId' starts...")
    val sparkContext = createSparkContext()
    val sparkSQLSession = createSparkSQLSession(sparkContext)
    val dataFrameStorage = new DataFrameStorageImpl

    val hostAddress: InetAddress = HostAddressResolver.findHostAddress()
    logger.info("Host address: {}", hostAddress.getHostAddress)

    val tempPath = Unzip.unzipAll(depsZip)

    val pythonPathGenerator = pyspark.Loader.load
      .map(new PythonPathGenerator(_))
      .getOrElse(throw new RuntimeException("Could not find PySpark!"))

    val pythonBinary = {
      def pythonBinaryDefault = ConfigFactory.load
        .getString("pythoncaretaker.python-binary-default")
      pythonBinaryPath.getOrElse(pythonBinaryDefault)
    }

    val operationExecutionDispatcher = new OperationExecutionDispatcher

    val customCodeEntryPoint = new CustomCodeEntryPoint(sparkContext,
      sparkSQLSession, dataFrameStorage, operationExecutionDispatcher)

    val pythonExecutionCaretaker = new PythonExecutionCaretaker(
      s"$tempPath/pyexecutor/pyexecutor.py",
      pythonPathGenerator,
      pythonBinary,
      sparkContext,
      sparkSQLSession,
      dataFrameStorage,
      customCodeEntryPoint,
      hostAddress)
    pythonExecutionCaretaker.start()

    val rExecutionCaretaker = new RExecutionCaretaker(s"$tempPath/r_executor.R",
      customCodeEntryPoint)
    rExecutionCaretaker.start()

    val customCodeExecutionProvider = CustomCodeExecutionProvider(
      pythonExecutionCaretaker.pythonCodeExecutor,
      rExecutionCaretaker.rCodeExecutor,
      operationExecutionDispatcher)

    implicit val system = ActorSystem()
    setupLivyKeepAliveLogging(system, keepAliveInterval)
    val workflowManagerClientActor = system.actorOf(
      WorkflowManagerClientActor.props(
        workflowOwnerId,
        wmUsername,
        wmPassword,
        wmAddress,
        wmWorkflowsPath,
        wmReportsPath,
        graphReader))

    val communicationFactory: MQCommunicationFactory = createCommunicationFactory(system)

    val workflowsSubscriberActor: ActorRef = createWorkflowsSubscriberActor(
      sparkContext,
      sparkSQLSession,
      dOperableCatalog,
      dataFrameStorage,
      customCodeExecutionProvider,
      system,
      workflowManagerClientActor,
      communicationFactory)

    val workflowsSubscriberReady = communicationFactory.registerSubscriber(
      MQCommunication.Topic.allWorkflowsSubscriptionTopic(workflowId),
      workflowsSubscriberActor)

    waitUntilSubscribersAreReady(Seq(workflowsSubscriberReady))

    val kernelManagerCaretaker = new KernelManagerCaretaker(
      system,
      pythonBinary,
      pythonPathGenerator,
      communicationFactory,
      tempPath,
      hostAddress.getHostAddress,
      pythonExecutionCaretaker.gatewayListeningPort.get,
      hostAddress.getHostAddress,
      rExecutionCaretaker.backendListeningPort,
      messageQueueHost,
      messageQueuePort,
      messageQueueUser,
      messageQueuePass,
      // TODO: Currently sessionId == workflowId
      workflowId,
      workflowIdObject
    )

    kernelManagerCaretaker.start()

    logger.info(s"Sending Init() to WorkflowsSubscriberActor")
    workflowsSubscriberActor ! Init()

    AkkaUtils.awaitTermination(system)
    cleanup(system, sparkContext, pythonExecutionCaretaker, kernelManagerCaretaker)
    logger.debug("SessionExecutor ends")
    System.exit(0)
  }

  private def createWorkflowsSubscriberActor(
      sparkContext: SparkContext,
      sparkSQLSession: SparkSQLSession,
      dOperableCatalog: DOperableCatalog,
      dataFrameStorage: DataFrameStorageImpl,
      customCodeExecutionProvider: CustomCodeExecutionProvider,
      system: ActorSystem,
      workflowManagerClientActor: ActorRef,
      communicationFactory: MQCommunicationFactory): ActorRef = {

    def createHeartbeatPublisher: ActorRef = {
      val seahorsePublisher = communicationFactory.createPublisher(
        // TODO: Currently sessionId == workflowId
        MQCommunication.Topic.seahorsePublicationTopic(workflowId),
        MQCommunication.Actor.Publisher.seahorse)

      val heartbeatWorkflowBroadcaster = communicationFactory.createBroadcaster(
        MQCommunication.Exchange.heartbeats(workflowIdObject),
        MQCommunication.Actor.Publisher.heartbeat(workflowIdObject)
      )

      val heartbeatAllBroadcaster = communicationFactory.createBroadcaster(
        MQCommunication.Exchange.heartbeatsAll,
        MQCommunication.Actor.Publisher.heartbeatAll
      )

      val routeePaths = scala.collection.immutable
        .Iterable(
          seahorsePublisher,
          heartbeatWorkflowBroadcaster,
          heartbeatAllBroadcaster)
        .map(_.path.toString)

      val heartbeatPublisher = system.actorOf(
        Props.empty.withRouter(BroadcastGroup(routeePaths)),
        "heartbeatBroadcastingRouter"
      )
      heartbeatPublisher
    }

    val heartbeatPublisher: ActorRef = createHeartbeatPublisher

    val emailSender = {
      val emailSenderConfig = EmailSenderConfig(
        smtpHost = mailServerHost,
        smtpPort = mailServerPort,
        from = mailServerSender,
        authorizationConfig = Some(EmailSenderAuthorizationConfig(
          user = mailServerUser,
          password = mailServerPassword)))
      EmailSender(emailSenderConfig)
    }
    val notebooksClientFactory = new NotebooksClientFactory(notebookServerAddress, 1 second, 3600)(system)

    // TODO There might be need to have it passed to we.jar as argument eventually
    val libraryPath = "/library"

    val executionContext = createExecutionContext(
      dataFrameStorage = dataFrameStorage,
      executionMode = ExecutionMode.Interactive,
      notebooksClientFactory = Some(notebooksClientFactory),
      emailSender = Some(emailSender),
      datasourceClientFactory = new DatasourceRestClientFactory(
        datasourceServerAddress, workflowOwnerId),
      customCodeExecutionProvider = customCodeExecutionProvider,
      sparkContext = sparkContext,
      sparkSQLSession = sparkSQLSession,
      tempPath = tempPath,
      libraryPath = libraryPath,
      dOperableCatalog = Some(dOperableCatalog))

    val readyBroadcaster = communicationFactory.createBroadcaster(
      MQCommunication.Exchange.ready(workflowIdObject),
      MQCommunication.Actor.Publisher.ready(workflowIdObject))

    val publisher: ActorRef = communicationFactory.createPublisher(
      // TODO: Currently sessionId == workflowId
      MQCommunication.Topic.workflowPublicationTopic(workflowIdObject, workflowId),
      MQCommunication.Actor.Publisher.workflow(workflowIdObject))

    val actorProvider = new SessionWorkflowExecutorActorProvider(
      executionContext,
      workflowManagerClientActor,
      heartbeatPublisher,
      readyBroadcaster,
      workflowManagerTimeout,
      publisher,
      // TODO: Currently sessionId == workflowId
      workflowId,
      heartbeatInterval)

    val workflowsSubscriberActor = system.actorOf(
      WorkflowTopicSubscriber.props(
        actorProvider,
        // TODO: Currently sessionId == workflowId
        workflowId,
        workflowIdObject),
      MQCommunication.Actor.Subscriber.workflows)

    workflowsSubscriberActor
  }

  private def createCommunicationFactory(system: ActorSystem): MQCommunicationFactory = {
    val connection: ActorRef = createConnection(system)
    val messageDeserializer = ProtocolJsonDeserializer(graphReader).orElse(GlobalMQDeserializer)
    val messageSerializer = ProtocolJsonSerializer(graphReader).orElse(GlobalMQSerializer)
    MQCommunicationFactory(system, connection, messageSerializer, messageDeserializer)
  }

  private def createConnection(system: ActorSystem): ActorRef = {
    val factory = new ConnectionFactory()
    factory.setHost(messageQueueHost)
    factory.setPort(messageQueuePort)
    factory.setUsername(messageQueueUser)
    factory.setPassword(messageQueuePass)
    system.actorOf(
      ConnectionActor.props(factory),
      MQCommunication.mqActorSystemName)
  }

  // Clients after receiving ready or heartbeat will assume
  // that we are listening for their response
  private def waitUntilSubscribersAreReady[T](subscribers: Seq[Future[T]]): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val subscribed: Future[Seq[T]] = Future.sequence(subscribers)
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
    AkkaUtils.terminate(system)
    logger.debug("Akka terminated!")
  }
}
