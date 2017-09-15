/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.schedule

import scala.concurrent.Future

import io.deepsense.commons.mail.{EmailSender, EmailSenderConfig}
import io.deepsense.seahorse.scheduling.SchedulingManagerConfig

private[schedule] object EmailSenderApi {
  private[this] val emailSenderConfig = EmailSenderConfig(SchedulingManagerConfig.config)
  private[this] val sender = EmailSender(emailSenderConfig)
  def sendEmail(title: String, body: String, sendTo: String): Future[Unit] = {
    val msg = sender.createPlainMessage(title, body, Seq(sendTo))
    sender.sendEmail(msg).map(throw _)
    Future.successful(())
  }
}
