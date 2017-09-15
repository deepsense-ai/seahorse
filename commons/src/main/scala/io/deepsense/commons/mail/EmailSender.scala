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

import javax.mail.Message.RecipientType
import javax.mail.internet.{InternetAddress, MimeMessage}
import javax.mail.{Address, Message, Session}

import scala.util.{Failure, Success, Try}

import com.sun.mail.smtp.SMTPTransport

import io.deepsense.commons.mail.templates.{TemplateInstanceToLoad, Template}
import io.deepsense.commons.utils.Logging

class EmailSender(emailSenderConfig: EmailSenderConfig) extends Logging {

  import EmailSender._

  def this() = {
    this(EmailSenderConfig())
  }

  val session: Session = Session.getInstance(emailSenderConfig.sessionProperties)

  private def createMessage(subject: String, to: Seq[String]): MimeMessage = {
    val msg = new MimeMessage(session)

    msg.setSubject(subject)

    val toAddresses = to.flatMap(InternetAddress.parse(_, false).asInstanceOf[Array[Address]])
    msg.setRecipients(RecipientType.TO, toAddresses.toArray)

    msg
  }

  def createTextMessage(subject: String,
      text: String,
      subtype: String,
      to: Seq[String]): Message = {
    val msg = createMessage(subject, to)
    msg.setText(
      text,
      // Charset null means it will be determined by JavaMail
      // by scanning the email body and calculating the ratio of
      // non-ASCII characters to total length of the text.
      // This is not documented, but can be easily verified
      // by following the subsequent calls that are made in
      // the library source code.
      // Also, as the documentation says:
      // "Note that there may be a performance penalty if
      // <code>text</code> is large, since this method may have
      // to scan all the characters to determine what charset to
      // use."
      null,
      subtype)

    msg
  }

  def createPlainMessage(subject: String, text: String, to: Seq[String]): Message = {
    createTextMessage(subject, text, "plain", to)
  }

  def createHtmlMessage(subject: String, html: String, to: Seq[String]): Message = {
    createTextMessage(subject, html, "html", to)
  }

  def createHtmlMessageFromTemplate[T : Template](subject: String,
      templateInstance: TemplateInstanceToLoad,
      to: Seq[String]) : Try[Message] = {

    val TemplateInstanceToLoad(templateName, templateContext) = templateInstance

    for {
      template <- implicitly[Template[T]].loadTemplate(templateName)
      html = implicitly[Template[T]].renderTemplate(template, templateContext)
    } yield {
      createHtmlMessage(subject, html, to)
    }
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
