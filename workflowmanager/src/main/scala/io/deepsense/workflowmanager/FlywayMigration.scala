/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.workflowmanager

import com.typesafe.config.ConfigFactory
import org.flywaydb.core.Flyway

object FlywayMigration {

  private val config = ConfigFactory.load

  def run(): Unit = {
    val flyway = new Flyway
    flyway.setBaselineOnMigrate(true)
    flyway.setLocations("db.migration.workflowmanager")
    flyway.setDataSource(config.getString("db.url"), "", "")
    flyway.migrate()
  }
}
