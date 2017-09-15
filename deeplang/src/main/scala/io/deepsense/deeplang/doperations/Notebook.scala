/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.deeplang.doperations

import scala.reflect.runtime.{universe => ru}

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.{DOperation, DOperation1To0}
import io.deepsense.deeplang.documentation.OperationDocumentation
import io.deepsense.deeplang.params.choice.{Choice, MultipleChoiceParam}
import io.deepsense.deeplang.params.{Params, StringParam}


abstract class Notebook()
  extends DOperation1To0[DataFrame] with Params with OperationDocumentation {

  import Notebook._

  // TODO: invent a better implementation of nested parameters
  val shouldExecuteParam = MultipleChoiceParam[SendEmailChoice](
    name = "execute notebook",
    description = "Should the Notebook cells be run when this operation is executed?"
  )
  setDefault(shouldExecuteParam, Set.empty: Set[SendEmailChoice])

  def getShouldExecute: Set[SendEmailChoice] = $(shouldExecuteParam)

  def setShouldExecute(emailChoice: Set[SendEmailChoice]): this.type =
    set(shouldExecuteParam, emailChoice)

  override val params: Array[io.deepsense.deeplang.params.Param[_]] =
    Array(shouldExecuteParam)

  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}


object Notebook {
  sealed trait SendEmailChoice extends Choice {
    override val name = ""

    val sendEmailParam = MultipleChoiceParam[EmailAddressChoice](
      name = "send e-mail report",
      description = "Should the e-mail report be sent after Notebook execution?"
    )
    setDefault(sendEmailParam, Set.empty: Set[EmailAddressChoice])

    def getSendEmail: Set[EmailAddressChoice] = $(sendEmailParam)

    def setSendEmail(emailAddressChoice: Set[EmailAddressChoice]): this.type =
      set(sendEmailParam, emailAddressChoice)

    override val params: Array[io.deepsense.deeplang.params.Param[_]] =
      Array(sendEmailParam)

    override val choiceOrder: List[Class[_ <: Choice]] = List(SendEmailChoice.getClass)
  }

  object SendEmailChoice extends SendEmailChoice


  sealed trait EmailAddressChoice extends Choice {
    override val name = ""

    val emailAddressParam = StringParam(
      name = "email address",
      description = "The address to which the report will be sent."
    )

    def getEmailAddress: String = $(emailAddressParam)

    def setEmailAddress(address: String): this.type =
      set(emailAddressParam, address)

    override val params: Array[io.deepsense.deeplang.params.Param[_]] =
      Array(emailAddressParam)

    override val choiceOrder: List[Class[_ <: Choice]] = List(EmailAddressChoice.getClass)
  }

  object EmailAddressChoice extends EmailAddressChoice
}
