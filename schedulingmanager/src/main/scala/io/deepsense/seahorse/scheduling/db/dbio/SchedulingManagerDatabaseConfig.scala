/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.db.dbio

import com.typesafe.config.Config

import io.deepsense.commons.service.db.DatabaseConfig

class SchedulingManagerDatabaseConfig(databaseConfig: Config) extends DatabaseConfig(databaseConfig) {
  val quartzSchema = databaseConfig.getString("quartzSchema")
}
