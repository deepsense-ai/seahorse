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

package io.deepsense.workflowexecutor

import akka.actor.{ActorSystem, Props}
import com.rabbitmq.client.ConnectionFactory
import com.thenewmotion.akka.rabbitmq.ConnectionActor
import org.apache.spark.{SparkConf, SparkContext}
import scopt.OptionParser

import io.deepsense.commons.BuildInfo
import io.deepsense.commons.utils.{Logging, Version}
import io.deepsense.deeplang.CatalogRecorder
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.deeplang.doperables.ReportLevel
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.workflowexecutor.communication.{MQCommunication, ProtocolDeserializer}
import io.deepsense.workflowexecutor.rabbitmq.{MQCommunicationFactory, MQPublisher, MySubscriber, SubscriberActor}

object SessionExecutorApp
  extends Logging {

  val graphReader = new GraphReader(dOperationsCatalog())

  private val parser: OptionParser[ExecutionParams]
  = new scopt.OptionParser[ExecutionParams](BuildInfo.name) {
    head(BuildInfo.toString)

    note("Workflow input:")
    opt[String]('m', "--message-queue-host") valueName "HOST" action {
      (x, c) => c.copy(messageQueueHost = Some(x))
    } text "workflow filename"

    help("help") text "print this help message and exit"
    version("version") text "print product version and exit"
    note("")
    note("Visit https://seahorse.deepsense.io for more details")

    checkConfig { c =>
      if (c.messageQueueHost.isEmpty) {
        failure("--message-queue-host is required")
      } else {
        success
      }
    }
  }

  def currentVersion: Version =
    Version(BuildInfo.apiVersionMajor, BuildInfo.apiVersionMinor, BuildInfo.apiVersionPatch)

  def main(args: Array[String]): Unit = {
    val cmdParams = parser.parse(args, ExecutionParams())
    if (cmdParams.isEmpty) {
      System.exit(1)
    }
    val params = cmdParams.get


    val dOperableCatalog = new DOperableCatalog
    CatalogRecorder.registerDOperables(dOperableCatalog)

    implicit val system = ActorSystem()
    val statusLogger = system.actorOf(Props[StatusLoggingActor], "status-logger")

    val executionDispatcher = system.actorOf(ExecutionDispatcherActor.props(
      createSparkContext(),
      dOperableCatalog,
      ReportLevel.HIGH,
      statusLogger), "workflows")

    val factory = new ConnectionFactory()
    factory.setHost(params.messageQueueHost.get)

    val connection = system.actorOf(
      ConnectionActor.props(factory),
      MQCommunication.mqActorSystemName)
    val exchange = "seahorse"

    val mySubscriber = system.actorOf(MySubscriber.props(executionDispatcher), "mySubscriber")
    val communicationFactory = MQCommunicationFactory(system, connection)

    val globalMessageDeserializer = ProtocolDeserializer(graphReader, currentVersion)
    val globalSubscriber = SubscriberActor(mySubscriber, globalMessageDeserializer)

    val globalPublisher: MQPublisher = communicationFactory.createCommunicationChannel(
      exchange, globalSubscriber)

    system.awaitTermination()
  }

  private def dOperationsCatalog(): DOperationsCatalog = {
    val catalog = DOperationsCatalog()
    CatalogRecorder.registerDOperations(catalog)
    catalog
  }

  private def createSparkContext(): SparkContext = {
    val sparkConf = new SparkConf()
    sparkConf.setAppName("Seahorse Workflow Executor")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .registerKryoClasses(Array())

    new SparkContext(sparkConf)
  }

  private case class ExecutionParams(
    messageQueueHost: Option[String] = None
  )
}
