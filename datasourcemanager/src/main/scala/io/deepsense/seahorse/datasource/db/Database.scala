/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.datasource.db

import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

import io.deepsense.seahorse.datasource.Configs

object Database {

  val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("databaseSlick", Configs.c)

  val db = dbConfig.db
  val driver: JdbcProfile = dbConfig.driver
  val api = driver.api

  def forceInitialization(): Unit = {
    // Force initialization here to work around bug https://github.com/slick/slick/issues/1400
    val session = db.createSession()
    try session.force() finally session.close()
  }

}
