/**
 * Copyright 2016, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.commons.mail

import java.util.Properties

import com.typesafe.config.{Config, ConfigFactory}

class EmailSenderConfig private (val config: Config) {

  import EmailSenderConfig._

  val emailSenderConf: Config = config.getConfig(emailSenderKey)

  val smtpHost: String = emailSenderConf.getString(smtpHostKey)
  val smtpPort: Int = emailSenderConf.getInt(smtpPortKey)

  val from: String = emailSenderConf.getString(fromKey)

  val sessionProperties: Properties = {
    val res = new Properties()
    res.put("mail.smtp.host", smtpHost)
    res.put("mail.smtp.port", smtpPort.toString)
    res.put("mail.from", from)
    res
  }

}

object EmailSenderConfig {
  def apply(config: Config): EmailSenderConfig = new EmailSenderConfig(config)
  def apply(): EmailSenderConfig = new EmailSenderConfig(ConfigFactory.load())

  val smtpHostKey = "smtp.host"
  val smtpPortKey = "smtp.port"
  val emailSenderKey = "email-sender"
  val fromKey = "from"
}
