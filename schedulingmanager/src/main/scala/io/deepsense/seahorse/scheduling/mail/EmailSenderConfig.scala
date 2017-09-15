/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.mail

import java.util.Properties

import com.typesafe.config.{Config, ConfigFactory}

class EmailSenderConfig(val config: Config) {

  import EmailSenderConfig._

  def this() = {
    this(ConfigFactory.load())
  }

  lazy val emailSenderConf = config.getConfig("email-sender")

  lazy val smtpHost = emailSenderConf.getString(smtpHostKey)
  lazy val smtpPort = emailSenderConf.getInt(smtpPortKey)

  lazy val from = emailSenderConf.getString("from")

  lazy val sessionProperties: Properties = {
    val res = new Properties()
    res.put("mail.smtp.host", smtpHost)
    res.put("mail.smtp.port", smtpPort.toString)
    res.put("mail.from", from)
    res
  }

}

object EmailSenderConfig {
  def apply(config: Config): EmailSenderConfig = new EmailSenderConfig(config)
  def apply(): EmailSenderConfig = new EmailSenderConfig()

  val smtpHostKey = "smtp.host"
  val smtpPortKey = "smtp.port"
}
