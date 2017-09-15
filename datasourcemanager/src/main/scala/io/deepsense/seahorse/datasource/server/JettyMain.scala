/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.server

import java.time.temporal.TemporalUnit

import org.eclipse.jetty.server.handler.gzip.GzipHandler
import org.eclipse.jetty.server.{HttpConfiguration, HttpConnectionFactory, NetworkTrafficServerConnector, Server}
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

import io.deepsense.commons.utils.LoggerForCallerClass
import io.deepsense.seahorse.datasource.Configs
import io.deepsense.seahorse.datasource.db.{Database, FlywayMigration}

object JettyMain {

  val logger = LoggerForCallerClass()
  val applicationConf = Configs.Jetty

  def main(args: Array[String]): Unit = start(args)

  def start(args: Array[String]): Server = {
    Database.forceInitialization()
    FlywayMigration.run()

    val server: Server = new Server
    logger.info("Starting Jetty...")

    server setStopTimeout applicationConf.stopTimeout.toMillis
    server setStopAtShutdown true

    val httpConfig = new HttpConfiguration()
    httpConfig setSendDateHeader true
    httpConfig setSendServerVersion false

    val connector = new NetworkTrafficServerConnector(server, new HttpConnectionFactory(httpConfig))
    connector setPort applicationConf.port
    connector setSoLingerTime 0
    connector setIdleTimeout applicationConf.connectorIdleTimeout.toMillis
    server addConnector connector

    val webApp = new WebAppContext
    webApp setInitParameter (
      ScalatraListener.LifeCycleKey,
      classOf[ScalatraBootstrap].getCanonicalName
    )

    webApp setContextPath "/"
    webApp setResourceBase getClass.getClassLoader.getResource("webapp").toExternalForm
    webApp setEventListeners Array(new ScalatraListener)
    webApp setMaxFormContentSize Configs.Jetty.maxFormContentSize
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
