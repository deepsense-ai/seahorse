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

import akka.actor.{ActorRef, ActorSystem, Props}
import com.rabbitmq.client.ConnectionFactory
import com.thenewmotion.akka.rabbitmq.ConnectionActor
import org.apache.spark.SparkContext

import io.deepsense.deeplang.doperables.ReportLevel
import io.deepsense.deeplang.doperables.ReportLevel._
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.workflowexecutor.communication.{MQCommunication, ProtocolDeserializer}
import io.deepsense.workflowexecutor.pythongateway.PythonGateway
import io.deepsense.workflowexecutor.rabbitmq._
import io.deepsense.workflowexecutor.{ExecutionDispatcherActor, StatusLoggingActor}

/**
 * SessionExecutor waits for user instructions in an infinite loop.
 */
case class SessionExecutor(
    reportLevel: ReportLevel,
    messageQueueHost: String)
  extends Executor {

  val graphReader = new GraphReader(createDOperationsCatalog())

  /**
   * WARNING: Performs an infinite loop.
   */
  def execute(): Unit = {
    logger.debug("SessionExecutor starts")
    val sparkContext = createSparkContext()
    val dOperableCatalog = createDOperableCatalog()

    implicit val system = ActorSystem()
    val statusLogger = system.actorOf(Props[StatusLoggingActor], "status-logger")

    val pythonGateway = PythonGateway(PythonGateway.GatewayConfig(), sparkContext)
    pythonGateway.start()

    val executionDispatcher = system.actorOf(ExecutionDispatcherActor.props(
      sparkContext,
      dOperableCatalog,
      ReportLevel.HIGH,
      statusLogger), "workflows")

    val factory = new ConnectionFactory()
    factory.setHost(messageQueueHost)

    val connection = system.actorOf(
      ConnectionActor.props(factory),
      MQCommunication.mqActorSystemName)

    val messageDeserializer = ProtocolDeserializer(graphReader)
    val communicationFactory = MQCommunicationFactory(system, connection, messageDeserializer)

    def createSeahorseSubscriber(publisher: MQPublisher): ActorRef =
      system.actorOf(
        SeahorseChannelSubscriber.props(
          executionDispatcher, communicationFactory, publisher, pythonGateway),
        "communication")

    communicationFactory.createCommunicationChannel(
      MQCommunication.seahorseExchange, createSeahorseSubscriber _)

    system.awaitTermination()
    cleanup(sparkContext)
    logger.debug("SessionExecutor ends")
  }

  private def cleanup(sparkContext: SparkContext): Unit = {
    logger.debug("Cleaning up...")
    sparkContext.stop()
    logger.debug("Spark terminated!")
  }
}
