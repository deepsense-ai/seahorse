/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.db

import org.flywaydb.core.Flyway

import io.deepsense.seahorse.datasource.Configs

object FlywayMigration {

  private val db = Configs.Database

  def run(): Unit = {
    val flyway = new Flyway
    flyway.setLocations("db")
    flyway.setSchemas(db.schema)
    flyway.setDataSource(Configs.c.getString("databaseSlick.db.url"), "", "")
    flyway.migrate()
  }
}
