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

package ai.deepsense.commons.service.server

import org.eclipse.jetty.server.handler.gzip.GzipHandler
import org.eclipse.jetty.server.{HttpConfiguration, HttpConnectionFactory, NetworkTrafficServerConnector, Server}
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

import ai.deepsense.commons.utils.LoggerForCallerClass

object CommonJettyMain {

  val logger = LoggerForCallerClass()

  def startServer(
      contextPath: String,
      scalatraBootstrapClass: Class[_],
      webAppResourcePath: String,
      jettyConfig: JettyConfig
    ): Server = {
    val server: Server = new Server
    logger.info("Starting Jetty...")

    server setStopTimeout jettyConfig.stopTimeout.toMillis
    server setStopAtShutdown true

    val httpConfig = new HttpConfiguration()
    httpConfig setSendDateHeader true
    httpConfig setSendServerVersion false

    val connector = new NetworkTrafficServerConnector(server, new HttpConnectionFactory(httpConfig))
    connector setPort jettyConfig.port
    connector setSoLingerTime 0
    connector setIdleTimeout jettyConfig.connectorIdleTimeout.toMillis
    server addConnector connector

    val webApp = new WebAppContext
    webApp setInitParameter (
      ScalatraListener.LifeCycleKey,
      scalatraBootstrapClass.getCanonicalName
      )

    webApp setContextPath contextPath
    webApp setResourceBase getClass.getClassLoader.getResource(webAppResourcePath).toExternalForm
    webApp setEventListeners Array(new ScalatraListener)
    webApp setMaxFormContentSize jettyConfig.maxFormContentSize
    server insertHandler decorateWithGzipHandler(webApp)

    server.start()
    server
  }

  private def decorateWithGzipHandler(webApp: WebAppContext): GzipHandler = {
    val gzippedWebApp = new GzipHandler()
    gzippedWebApp.setIncludedMimeTypes(
      "text/css",
      "text/javascript",
      "text/html",
      "text/plain",
      "text/xml",
      "application/javascript",
      "application/json"
    )
    gzippedWebApp.setHandler(webApp)
    gzippedWebApp
  }

}
