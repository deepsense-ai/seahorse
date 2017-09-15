/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.libraryservice

import com.typesafe.config.ConfigFactory

object Config {
  private val config = ConfigFactory.load()

  object Server {
    private val serverConfig = config.getConfig("server")
    lazy val port = serverConfig.getInt("port")
    lazy val apiPrefix = serverConfig.getString("apiPrefix")
  }

  object Storage {
    private val storageConfig = config.getConfig("storage")
    lazy val directory = storageConfig.getString("directory")
  }
}
