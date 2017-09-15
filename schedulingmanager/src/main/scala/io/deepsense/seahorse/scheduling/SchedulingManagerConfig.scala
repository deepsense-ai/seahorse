/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling

import com.typesafe.config.ConfigFactory

import io.deepsense.commons.service.db.DatabaseConfig
import io.deepsense.commons.service.server.JettyConfig

object SchedulingManagerConfig {

  val config = ConfigFactory.load("jetty.default.conf").withFallback(
    ConfigFactory.load("database.default.conf")
  ).withFallback(
    ConfigFactory.defaultApplication()
  )

  val jetty = new JettyConfig(config.getConfig("jetty"))
  val database = new DatabaseConfig(config.getConfig("database"))

}
