/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager

import com.typesafe.config.ConfigFactory
import org.flywaydb.core.Flyway

object FlywayMigration {

  private val config = ConfigFactory.load

  def run(): Unit = {
    val flyway = new Flyway
    flyway.setBaselineOnMigrate(true)
    flyway.setDataSource(config.getString("db.url"), "", "")
    flyway.migrate()
  }
}
