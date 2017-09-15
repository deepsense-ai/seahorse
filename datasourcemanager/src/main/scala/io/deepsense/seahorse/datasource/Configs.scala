/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource

import com.typesafe.config.ConfigFactory

import io.deepsense.commons.utils.LoggerForCallerClass

object Configs {

  val logger = LoggerForCallerClass()

  val c = ConfigFactory.load()

  object Jetty {
    private val jettyConfig = c.getConfig("jetty")
    val port = jettyConfig.getInt("port")
    val stopTimeout = jettyConfig.getDuration("stopTimeout")
    val connectorIdleTimeout = jettyConfig.getDuration("connectorIdleTimeout")
    val maxFormContentSize = jettyConfig.getInt("maxFormContentSize")
  }

  object Database {
    private val databaseConfig = c.getConfig("database")

    val schema = databaseConfig.getString("schema")
    val timeout = databaseConfig.getDuration("timeout")
  }

}
