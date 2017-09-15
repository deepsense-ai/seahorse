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

package ai.deepsense.workflowexecutor.pythongateway

import java.net.InetAddress
import java.util.concurrent.atomic.AtomicReference

import scala.annotation.tailrec
import scala.concurrent.duration._

import org.apache.spark.SparkContext
import py4j._

import ai.deepsense.commons.utils.Logging
import ai.deepsense.deeplang._
import ai.deepsense.sparkutils.SparkSQLSession
import ai.deepsense.workflowexecutor.customcode.CustomCodeEntryPoint
import ai.deepsense.workflowexecutor.pythongateway.PythonGateway.GatewayConfig

case class PythonGateway(
                          gatewayConfig: GatewayConfig,
                          sparkContext: SparkContext,
                          sparkSQLSession: SparkSQLSession,
                          dataFrameStorage: DataFrameStorage,
                          pythonEntryPoint: CustomCodeEntryPoint,
                          hostAddress: InetAddress) extends Logging {
  import PythonGateway._

  private val gatewayStateListener = new GatewayEventListener
  private[pythongateway] val gatewayServer =
    createGatewayServer(pythonEntryPoint, gatewayStateListener)

  def start(): Unit = gatewayServer.start()
  def stop(): Unit = gatewayServer.shutdown()

  def codeExecutor: CustomCodeExecutor =
    pythonEntryPoint.getCodeExecutor(gatewayConfig.pyExecutorSetupTimeout)

  def listeningPort: Option[Int] =
    (gatewayServer.getListeningPort, gatewayStateListener.running) match {
      case (-1, _) => None
      case (_, false) => None
      case (p, true) => Some(p)
    }

  private def createGatewayServer(entryPoint: CustomCodeEntryPoint,
                                  listener: GatewayEventListener): GatewayServer = {

    val callbackClient = new LazyCallbackClient(
      () => entryPoint.getPythonPort(gatewayConfig.pyExecutorSetupTimeout), hostAddress)

    // It is quite important that these values are 0,
    // which translates to infinite timeout.
    // Non-zero values might lead to the server shutting down unexpectedly.
    val connectTimeout = 0
    val readTimeout = 0
    val port = 0 // Use a random available port.

    val gateway = GatewayServerFactory.create(
      entryPoint,
      port,
      connectTimeout,
      readTimeout,
      callbackClient,
      hostAddress)

    gateway.addListener(listener)

    gateway
  }
}

object PythonGateway {

  /**
   * A wrapper around Py4j's CallbackClient
   * that instantiates the actual CallbackClient lazily,
   * and re-instantiates it every time the callback port changes.
   *
   * This way we don't have to know Python's listening port
   * at the time of Gateway instantiation and are prepared
   * for restarting the callback server.
   */
  class LazyCallbackClient(
      val getCallbackPort: () => Int,
      val host: InetAddress) extends CallbackClient(0, host) {

    private val clientRef = new AtomicReference(new CallbackClient(0, host))

    override def sendCommand(command: String): String = {
      @tailrec
      def updateAndGet(): CallbackClient = {
        val port = getCallbackPort()
        val currentClient = clientRef.get()

        if (currentClient.getPort == port) {
          currentClient
        } else {
          val newClient = new CallbackClient(port, host)
          if (clientRef.compareAndSet(currentClient, newClient)) {
            currentClient.shutdown()
            newClient
          } else {
            updateAndGet()
          }
        }
      }

      updateAndGet().sendCommand(command)
    }

    override def shutdown(): Unit = {
      clientRef.get.shutdown()
      super.shutdown()
    }
  }

  class GatewayEventListener extends DefaultGatewayServerListener with Logging {
    var running: Boolean = true

    override def serverStopped(): Unit = {
      logger.info("Gateway server stopped")
      running = false
    }
  }

  case class GatewayConfig(
    pyExecutorSetupTimeout: Duration = 30.seconds)
}
