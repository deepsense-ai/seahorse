/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.commons.service.db

import com.typesafe.config.Config

class DatabaseConfig(databaseConfig: Config) {
  val schema = databaseConfig.getString("schema")
  val timeout = databaseConfig.getDuration("timeout")
}
