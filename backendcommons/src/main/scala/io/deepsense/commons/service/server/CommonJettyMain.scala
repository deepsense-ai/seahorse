/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.commons.service.server

import org.eclipse.jetty.server.handler.gzip.GzipHandler
import org.eclipse.jetty.server.{HttpConfiguration, HttpConnectionFactory, NetworkTrafficServerConnector, Server}
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

import io.deepsense.commons.utils.LoggerForCallerClass

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
