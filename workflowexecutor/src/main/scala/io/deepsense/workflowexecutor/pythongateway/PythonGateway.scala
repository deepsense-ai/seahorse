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

package io.deepsense.workflowexecutor.pythongateway

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

import org.apache.spark.SparkContext
import py4j.{CallbackClient, GatewayServer}

import io.deepsense.commons.utils.Logging
import io.deepsense.workflowexecutor.pythongateway.PythonGateway.GatewayConfig

case class PythonGateway(
    gatewayConfig: GatewayConfig,
    sparkContext: SparkContext) extends Logging {
  import PythonGateway._

  val entryPoint = new PythonEntryPoint(sparkContext)

  private[pythongateway] val gatewayServer = createGatewayServer(entryPoint)

  def start(): Unit = gatewayServer.start()
  def stop(): Unit = gatewayServer.shutdown()

  def listeningPort: Option[Int] =
    gatewayServer.getListeningPort match {
      case -1 => None
      case p => Some(p)
    }

  private def createGatewayServer(entryPoint: PythonEntryPoint): GatewayServer = {
    val callbackClient =
      new LazyCallbackClient(
        entryPoint.pythonPortPromise.future,
        gatewayConfig.callbackClientSetupTimeout)

    // It is quite important that these values are 0,
    // which translates to infinite timeout.
    // Non-zero values might lead to the server shutting down unexpectedly.
    val connectTimeout = 0
    val readTimeout = 0
    val port = 0 // Use a random available port.

    new GatewayServer(
      entryPoint,
      port,
      connectTimeout,
      readTimeout,
      null, // no custom commands
      callbackClient)
  }
}

object PythonGateway {

  /**
   * A wrapper around Py4j's CallbackClient
   * that instantiates the actual CallbackClient lazily.
   *
   * This way we don't have to know Python's listening port
   * at the time of Gateway instantiation.
   */
  class LazyCallbackClient(val port: Future[Int], val startupTimeout: Duration)
      extends CallbackClient(0)
      with Logging {

    private val clientImpl: Future[CallbackClient] =
      port map {
        case p: Int =>
          logger.debug("Creating callback client")
          new CallbackClient(p)
      }

    private def getClientImpl: CallbackClient = {
      logger.debug("Callback client requested")
      Await.result(clientImpl, startupTimeout)
    }

    override def sendCommand(command: String): String = {
      getClientImpl.sendCommand(command)
    }
  }

  case class GatewayConfig(
      callbackClientSetupTimeout: Duration = 5000 millis)
}
