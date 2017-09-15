/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource

import com.typesafe.config.ConfigFactory

import io.deepsense.commons.service.db.DatabaseConfig
import io.deepsense.commons.service.server.JettyConfig

object DatasourceManagerConfig {

  // TODO Load all *.default.conf automatically
  val config =
  ConfigFactory.defaultApplication().withFallback(
    ConfigFactory.load("database.default.conf")
  ).withFallback(
    ConfigFactory.load("jetty.default.conf")
  )

  val jetty = JettyConfig(config.getConfig("jetty"))
  val database = new DatabaseConfig(config.getConfig("database"))

  val predefinedUsersConfigPath = "predefined-users"
  val schedulerUserConfigPath = "scheduler"

}
