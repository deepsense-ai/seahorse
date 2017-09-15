/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.mail

import javax.mail.Message.RecipientType
import javax.mail.internet.{InternetAddress, MimeMessage}
import javax.mail.{Address, Message, Session}

import scala.util.{Failure, Success, Try}

import com.sun.mail.smtp.SMTPTransport

import io.deepsense.commons.utils.Logging

class EmailSender(emailSenderConfig: EmailSenderConfig) extends Logging {

  import EmailSender._

  def this() = {
    this(EmailSenderConfig())
  }

  val session = Session.getInstance(emailSenderConfig.sessionProperties)

  def createMessage(subject: String, text: String, to: String*): Message = {
    val msg = new MimeMessage(session)
    msg.setSubject(subject)
    msg.setText(text)
    val toaddresses = to.flatMap(InternetAddress.parse(_, false).asInstanceOf[Array[Address]])
    msg.setRecipients(RecipientType.TO, toaddresses.toArray)

    msg
  }

  def sendEmail(message: Message): Option[Throwable] = {
    val transport = session.getTransport("smtp").asInstanceOf[SMTPTransport]
    val sent = Try {
      transport.connect()
      transport.sendMessage(message, message.getAllRecipients)
    }

    transport.close()

    sent match {
      case Success(_) if logger.isDebugEnabled =>
        logger.debug(s"Mail sent to ${recipientsForLogging(message)}")
      case Failure(t) =>
        logger.error(s"Unable to send message to ${recipientsForLogging(message)}", t)
      case _ =>
    }

    sent.failed.toOption
  }
}

object EmailSender {
  def apply(emailSenderConfig: EmailSenderConfig): EmailSender = new EmailSender(emailSenderConfig)
  def apply(): EmailSender = new EmailSender()

  private def recipientsForLogging(msg: Message): String = {
    msg.getAllRecipients.mkString("[", ", ", "]")
  }
}
