/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.server

import com.typesafe.config.ConfigFactory
import org.eclipse.jetty.server.Server

import io.deepsense.commons.service.{CommonJettyMain, JettyConfig}

object JettyMain {

  def main(args: Array[String]): Unit = start(args)

  def start(args: Array[String]): Server = {
    val jettyConfig = new JettyConfig(ConfigFactory.load("jetty.default.conf").getConfig("jetty"))

    CommonJettyMain.startServer(
      contextPath = "/schedulingmanager/v1/",
      scalatraBootstrapClass = classOf[ScalatraBootstrap],
      webAppResourcePath = "scalatra-webapp",
      jettyConfig
    )
  }

}
