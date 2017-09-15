/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.server

import com.typesafe.config.ConfigFactory
import org.eclipse.jetty.server.Server

import io.deepsense.commons.service.server.{CommonJettyMain, JettyConfig}
import io.deepsense.seahorse.scheduling.SchedulingManagerConfig
import io.deepsense.seahorse.scheduling.db.{Database, FlywayMigration}

object JettyMain {

  def main(args: Array[String]): Unit = start(args, SchedulingManagerConfig.jetty)

  def start(args: Array[String], config: JettyConfig): Server = {
    Database.forceInitialization()
    FlywayMigration.run()

    CommonJettyMain.startServer(
      contextPath = "/schedulingmanager/v1/",
      scalatraBootstrapClass = classOf[ScalatraBootstrap],
      webAppResourcePath = "scalatra-webapp",
      config
    )
  }

}
