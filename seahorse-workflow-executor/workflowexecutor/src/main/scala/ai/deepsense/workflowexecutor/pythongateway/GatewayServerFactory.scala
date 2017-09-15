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

package ai.deepsense.workflowexecutor.pythongateway

import java.lang.reflect.{Field, Modifier}
import java.net.InetAddress
import java.util.concurrent.CopyOnWriteArrayList

import py4j.{CallbackClient, Gateway, GatewayServer, GatewayServerListener}

import ai.deepsense.sparkutils

object GatewayServerFactory {

  /**
   * GatewayServer is missing an appropriate constructor.
   * We have to use reflection to set it's private final fields to correct values.
   */
  def create(
      entryPoint: Object,
      port: Int,
      connectTimeout: Int,
      readTimeout: Int,
      cbClient: CallbackClient,
      hostAddress: InetAddress): GatewayServer = {
    val gatewayServer = new GatewayServer(())
    val clazz = gatewayServer.getClass

    def setFieldValue(fieldName: String, value: Any): Unit = {
      val field = clazz.getDeclaredField(fieldName)
      field.setAccessible(true)

      val modifiersField = classOf[Field].getDeclaredField("modifiers")
      modifiersField.setAccessible(true)
      modifiersField.setInt(field, field.getModifiers & ~Modifier.FINAL)
      field.set(gatewayServer, value)
    }
    // These fields are private. GatewayServer is missing an appropriate constructor.
    // We have to use reflection.
    // gatewayServer.port = port
    setFieldValue("port", port)

    // gatewayServer.connectTimeout = connectTimeout
    setFieldValue("connectTimeout", connectTimeout)

    // gatewayServer.readTimeout = readTimeout
    setFieldValue("readTimeout", readTimeout)

    if (sparkutils.PythonGateway.gatewayServerHasCallBackClient) {
      // gatewayServer.cbClient = cbClient
      setFieldValue("cbClient", cbClient)
    }

    // gatewayServer.gateway = new Gateway(entryPoint, cbClient)
    setFieldValue("gateway", new Gateway(entryPoint, cbClient))

    // gatewayServer.pythonPort = cbClient.getPort()
    setFieldValue("pythonPort", cbClient.getPort)

    // gatewayServer.pythonAddress = cbClient.getAddress()
    setFieldValue("pythonAddress", cbClient.getAddress)

    // gatewayServer.gateway.getBindings.put(GatewayServer.GATEWAY_SERVER_ID, this)
    val gatewayField = clazz.getDeclaredField("gateway")
    gatewayField.setAccessible(true)
    gatewayField.get(gatewayServer)
      .asInstanceOf[Gateway].getBindings.put(GatewayServer.GATEWAY_SERVER_ID, gatewayServer)

    // gatewayServer.customCommands = null
    setFieldValue("customCommands", null)

    // gatewayServer.listeners = new CopyOnWriteArrayList[GatewayServerListener]()
    setFieldValue("listeners", new CopyOnWriteArrayList[GatewayServerListener]())

    // gatewayServer.address = hostAddress
    setFieldValue("address", hostAddress)

    gatewayServer
  }
}
