/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling

import com.typesafe.config.ConfigFactory

import io.deepsense.commons.mail.EmailSenderConfig
import io.deepsense.commons.service.db.DatabaseConfig
import io.deepsense.commons.service.server.JettyConfig
import io.deepsense.seahorse.scheduling.db.dbio.SchedulingManagerDatabaseConfig

object SchedulingManagerConfig {

  val config =
    ConfigFactory.defaultApplication().withFallback(
      ConfigFactory.load("jetty.default.conf")
  ).withFallback(
      ConfigFactory.load("database.default.conf")
  ).resolve()

  val jetty = JettyConfig(config.getConfig("jetty"))
  val database = new SchedulingManagerDatabaseConfig(config.getConfig("database"))
  val emailSender = EmailSenderConfig(config.getConfig("email-sender"))

}
