/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.server

import org.eclipse.jetty.server.Server

import io.deepsense.commons.service.server.CommonJettyMain
import io.deepsense.seahorse.datasource.DatasourceManagerConfig
import io.deepsense.seahorse.datasource.db.{Database, FlywayMigration}

object JettyMain {

  def main(args: Array[String]): Unit = start(args)

  def start(args: Array[String]): Server = {
    Database.forceInitialization()
    FlywayMigration.run()

    CommonJettyMain.startServer(
      contextPath = "/datasourcemanager/v1/",
      scalatraBootstrapClass = classOf[ScalatraBootstrap],
      webAppResourcePath = "scalatra-webapp",
      DatasourceManagerConfig.jetty
    )
  }

}
