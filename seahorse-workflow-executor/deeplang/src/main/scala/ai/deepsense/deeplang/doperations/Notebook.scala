/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperations

import java.io.{ByteArrayInputStream, InputStream, PrintWriter, StringWriter}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.reflect.runtime.{universe => ru}

import ai.deepsense.deeplang.documentation.OperationDocumentation
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.params.choice.{Choice, MultipleChoiceParam}
import ai.deepsense.deeplang.params.{Params, StringParam}
import ai.deepsense.deeplang.{DOperation1To0, ExecutionContext}


abstract class Notebook()
  extends DOperation1To0[DataFrame] with Params with OperationDocumentation {

  import Notebook._

  // TODO: invent a better implementation of nested parameters
  val shouldExecuteParam = MultipleChoiceParam[SendEmailChoice](
    name = "execute notebook",
    description = Some("Should the Notebook cells be run when this operation is executed?")
  )
  setDefault(shouldExecuteParam, Set.empty: Set[SendEmailChoice])

  def getShouldExecute: Set[SendEmailChoice] = $(shouldExecuteParam)

  def setShouldExecute(emailChoice: Set[SendEmailChoice]): this.type =
    set(shouldExecuteParam, emailChoice)

  override val specificParams: Array[ai.deepsense.deeplang.params.Param[_]] =
    Array(shouldExecuteParam)

  val notebookType: String

  def headlessExecution(context: ExecutionContext) : Unit = {

    context.notebooksClient.map(_.as.dispatcher).foreach { implicit ec =>
      for {
        _ <- getShouldExecute
        generatedNotebookFutOpt = context.notebooksClient.map(_.generateAndPollNbData(notebookType))
        streamFut <- generatedNotebookFutOpt.map(_.map(new ByteArrayInputStream(_)))
      } {
        logger.info(s"Generating notebook data")

        streamFut.onFailure {
          case t =>
            val stackWriter = new StringWriter()
            t.printStackTrace(new PrintWriter(stackWriter))
            sendMail("Notebook execution failed", "Sorry! The execution of your notebook has failed.\n" +
              stackWriter.toString, context, None)
        }

        Await.result(for {
          stream <- streamFut
        } yield {
          sendMail("Notebook execution result",
            "Hi, please find the attached file with notebook execution result.",
            context,
            Some((stream, Some(Notebook.notebookDataMimeType)))
          )

        }, Duration.Inf)
      }
    }
  }

  private def sendMail(subject: String,
      body: String,
      context: ExecutionContext,
      attachment: Option[(InputStream, Option[String])]
  ): Unit = {
    for {
      shouldExecute <- getShouldExecute
      mailAddress <- shouldExecute.getSendEmail
      sender <- context.emailSender
      email = mailAddress.getEmailAddress
      msg = sender.createPlainMessage(subject, body, Seq(email))
      msgWithAttachment = attachment.map {
        case (stream, contentTypeOpt) =>
          sender.attachAttachment(msg, stream, Notebook.notebookDataFilename, contentTypeOpt)
      }.getOrElse(msg)
    } {
      sender.sendEmail(msgWithAttachment).foreach(throw _)
    }
  }

  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}


object Notebook {
  val notebookDataMimeType = "text/html"
  val notebookDataFilename = "notebook.html"

  sealed trait SendEmailChoice extends Choice {
    override val name = ""

    val sendEmailParam = MultipleChoiceParam[EmailAddressChoice](
      name = "send e-mail report",
      description = Some("Should the e-mail report be sent after Notebook execution?")
    )
    setDefault(sendEmailParam, Set.empty: Set[EmailAddressChoice])

    def getSendEmail: Set[EmailAddressChoice] = $(sendEmailParam)

    def setSendEmail(emailAddressChoice: Set[EmailAddressChoice]): this.type =
      set(sendEmailParam, emailAddressChoice)

    override val params: Array[ai.deepsense.deeplang.params.Param[_]] =
      Array(sendEmailParam)

    override val choiceOrder: List[Class[_ <: Choice]] = List(SendEmailChoice.getClass)
  }

  object SendEmailChoice extends SendEmailChoice


  sealed trait EmailAddressChoice extends Choice {
    override val name = ""

    val emailAddressParam = StringParam(
      name = "email address",
      description = Some("The address to which the report will be sent.")
    )

    def getEmailAddress: String = $(emailAddressParam)

    def setEmailAddress(address: String): this.type =
      set(emailAddressParam, address)

    override val params: Array[ai.deepsense.deeplang.params.Param[_]] =
      Array(emailAddressParam)

    override val choiceOrder: List[Class[_ <: Choice]] = List(EmailAddressChoice.getClass)
  }

  object EmailAddressChoice extends EmailAddressChoice
}


