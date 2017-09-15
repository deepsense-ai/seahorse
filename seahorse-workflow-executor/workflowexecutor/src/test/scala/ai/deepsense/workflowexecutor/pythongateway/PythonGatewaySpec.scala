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

import java.io.PrintStream
import java.net.{InetAddress, ServerSocket, Socket}

import scala.concurrent.duration
import scala.concurrent.duration.FiniteDuration
import scala.io.BufferedSource
import scala.util.{Success, Try}
import org.apache.spark.SparkContext
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.Eventually._
import org.scalatest.concurrent.{TimeLimits, Timeouts}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.SpanSugar._
import org.scalatest.{Matchers, WordSpec}

import ai.deepsense.deeplang.DataFrameStorage
import ai.deepsense.sparkutils.SparkSQLSession
import ai.deepsense.workflowexecutor.customcode.CustomCodeEntryPoint
import ai.deepsense.workflowexecutor.pythongateway.PythonGateway.GatewayConfig


class PythonGatewaySpec extends WordSpec with MockitoSugar with Matchers with TimeLimits {

  val gatewayConfig = {
    GatewayConfig(FiniteDuration(500, duration.MILLISECONDS))
  }

  def attemptConnection(port: Option[Int]): Try[Socket] =
    port.fold {
      Try[Socket] { throw new IllegalStateException("Listening port should be present") }
    }{
      port => Try[Socket] { new Socket("127.0.0.1", port) }
    }

  "Gateway" should {
    val localhost = InetAddress.getByName("127.0.0.1")

    "set up a listening port" in {
      val gateway = PythonGateway(
        gatewayConfig,
        mock[SparkContext],
        mock[SparkSQLSession],
        mock[DataFrameStorage],
        mock[CustomCodeEntryPoint],
        localhost)
      gateway.start()

      val connectionAttempt = attemptConnection(gateway.listeningPort)
      connectionAttempt shouldBe a [Success[_]]

      gateway.stop()
    }

    "return None when stopped and asked for its listening port" in {
      val gateway = PythonGateway(
        gatewayConfig,
        mock[SparkContext],
        mock[SparkSQLSession],
        mock[DataFrameStorage],
        mock[CustomCodeEntryPoint],
        localhost)
      gateway.start()
      gateway.stop()
      eventually (timeout(5.seconds), interval(400.millis)) {
        gateway.listeningPort shouldBe None
      }
    }

    "return None when not started and asked for its listening port" in {
      val gateway = PythonGateway(
        gatewayConfig,
        mock[SparkContext],
        mock[SparkSQLSession],
        mock[DataFrameStorage],
        mock[CustomCodeEntryPoint],
        localhost)

      gateway.listeningPort shouldBe None
    }

    "send a message on initialized callback client" in {

      val customCodeEntryPoint = mock[CustomCodeEntryPoint]
      val gateway = PythonGateway(
        gatewayConfig,
        mock[SparkContext],
        mock[SparkSQLSession],
        mock[DataFrameStorage],
        customCodeEntryPoint,
        localhost)


      gateway.start()

      val command = "Hello!"
      val response = "Hello back!"

      // This thread acts as Python callback server
      val callbackServerSocket = new ServerSocket(0)
      val callbackServer = new Thread(new Runnable {
        override def run(): Unit = {
          val s = callbackServerSocket.accept()
          val message = new BufferedSource(s.getInputStream)
            .iter
            .take(command.length)
            .foldLeft("") {
              case (s: String, c: Char) => s + c
            }
          message shouldBe command
          new PrintStream(s.getOutputStream).print(response + "\n")
        }
      })

      callbackServer.setDaemon(true)
      callbackServer.start()

      when(customCodeEntryPoint.getPythonPort(any())).thenReturn(callbackServerSocket.getLocalPort)

      // This is run inside a separate thread, because failAfter doesn't seem to work otherwise
      var serverResponse: String = ""
      failAfter(1000.millis) {
        val t = new Thread(new Runnable {
          override def run(): Unit =
            serverResponse = gateway.gatewayServer.getCallbackClient.sendCommand(command)
        })
        t.start()
        t.join()
      }

      serverResponse shouldBe response

      gateway.stop()
    }
  }
}
