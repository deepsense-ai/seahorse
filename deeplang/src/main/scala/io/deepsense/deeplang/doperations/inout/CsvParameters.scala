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

package io.deepsense.deeplang.doperations.inout

import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang.params.choice.{Choice, ChoiceParam}
import io.deepsense.deeplang.params.{BooleanParam, Params, StringParam}

trait CsvParameters {
  this: Params =>

  import CsvParameters._

  val csvColumnSeparator = ChoiceParam[ColumnSeparatorChoice](
    name = "separator",
    description = "Column separator")
  setDefault(csvColumnSeparator, ColumnSeparatorChoice.Comma())

  def getCsvColumnSeparator: ColumnSeparatorChoice = $(csvColumnSeparator)
  def setCsvColumnSeparator(value: ColumnSeparatorChoice): this.type =
    set(csvColumnSeparator, value)

  val csvNamesIncluded = BooleanParam(
    name = "names included",
    description = "Does the first row include column names?")
  setDefault(csvNamesIncluded, true)

  def getCsvNamesIncluded: Boolean = $(csvNamesIncluded)
  def setCsvNamesIncluded(value: Boolean): this.type = set(csvNamesIncluded, value)

  def determineColumnSeparator(): Char = {
    getCsvColumnSeparator match {
      case ColumnSeparatorChoice.Comma() => ','
      case ColumnSeparatorChoice.Semicolon() => ';'
      case ColumnSeparatorChoice.Tab() => '\t'
      case ColumnSeparatorChoice.Colon() => ':'
      case ColumnSeparatorChoice.Space() => ' '
      case (customChoice: ColumnSeparatorChoice.Custom) =>
        customChoice.getCustomColumnSeparator(0)
    }
  }
}

object CsvParameters {

  sealed trait ColumnSeparatorChoice extends Choice {
    import ColumnSeparatorChoice._

    override val choiceOrder: List[Class[_ <: ColumnSeparatorChoice]] = List(
      classOf[Comma],
      classOf[Semicolon],
      classOf[Colon],
      classOf[Space],
      classOf[Tab],
      classOf[Custom])
  }

  object ColumnSeparatorChoice {

    case class Comma() extends ColumnSeparatorChoice {
      override val name = ","
    }
    case class Semicolon() extends ColumnSeparatorChoice {
      override val name = ";"
    }
    case class Colon() extends ColumnSeparatorChoice {
      override val name = ":"
    }
    case class Space() extends ColumnSeparatorChoice {
      override val name = "Space"
    }
    case class Tab() extends ColumnSeparatorChoice {
      override val name = "Tab"
    }
    case class Custom() extends ColumnSeparatorChoice {
      override val name = "Custom"

      val customColumnSeparator = StringParam(
        name = "custom separator",
        description = "Custom column separator",
        validator = new SingleCharRegexValidator)
      setDefault(customColumnSeparator, ",")

      def getCustomColumnSeparator: String = $(customColumnSeparator)
      def setCustomColumnSeparator(value: String): this.type = set(customColumnSeparator, value)
    }
  }
}
