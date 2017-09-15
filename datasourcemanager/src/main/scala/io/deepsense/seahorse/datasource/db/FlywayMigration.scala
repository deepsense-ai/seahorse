/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.db

import org.flywaydb.core.Flyway

import io.deepsense.seahorse.datasource.DatasourceManagerConfig

object FlywayMigration {

  private val db = DatasourceManagerConfig.database

  def run(): Unit = {
    val flyway = new Flyway
    flyway.setLocations("db.migration.datasourcemanager")
    flyway.setSchemas(db.schema)
    flyway.setDataSource(DatasourceManagerConfig.config.getString("databaseSlick.db.url"), "", "")
    flyway.migrate()
  }
}
