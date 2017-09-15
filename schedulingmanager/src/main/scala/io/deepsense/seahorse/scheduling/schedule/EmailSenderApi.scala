/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.schedule

import scala.concurrent.Future

import io.deepsense.seahorse.scheduling.SchedulingManagerConfig
import io.deepsense.seahorse.scheduling.mail.{EmailSender, EmailSenderConfig}

private[schedule] object EmailSenderApi {
  private[this] val emailSenderConfig = EmailSenderConfig(SchedulingManagerConfig.config)
  private[this] val sender = EmailSender(emailSenderConfig)
  def sendEmail(title: String, body: String, sendTo: String): Future[Unit] = {
    val msg = sender.createMessage(title, body, sendTo)
    sender.sendEmail(msg).map(throw _)
    Future.successful(())
  }
}
