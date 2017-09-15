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

import java.util.NoSuchElementException

import scala.collection.immutable.ListMap

import io.deepsense.deeplang.parameters._

trait CsvParameters {

  import CsvParameters._

  val csvCustomColumnSeparatorParameter = StringParameter(
    "Custom column separator",
    default = Some(","),
    validator = new SingleCharRegexValidator)

  val csvColumnSeparatorParameter = ChoiceParameter(
    "Column separator",
    default = Some(ColumnSeparator.COMMA.toString),
    options = ListMap(
      ColumnSeparator.COMMA.toString -> ParametersSchema(),
      ColumnSeparator.SEMICOLON.toString -> ParametersSchema(),
      ColumnSeparator.COLON.toString -> ParametersSchema(),
      ColumnSeparator.SPACE.toString -> ParametersSchema(),
      ColumnSeparator.TAB.toString -> ParametersSchema(),
      ColumnSeparator.CUSTOM.toString -> ParametersSchema(
        ColumnSeparator.CUSTOM.toString -> csvCustomColumnSeparatorParameter)))

  val csvNamesIncludedParameter = BooleanParameter(
    description = "Does the first row include column names?",
    default = Some(true)
  )

  def determineColumnSeparator(): Char = {
    val fieldSeparator = try {
      ColumnSeparator.withName(csvColumnSeparatorParameter.value)
    } catch {
      // Comma by default
      case e: NoSuchElementException => ColumnSeparator.COMMA
    }

    fieldSeparator match {
      case ColumnSeparator.COMMA => ','
      case ColumnSeparator.SEMICOLON => ';'
      case ColumnSeparator.COLON => ':'
      case ColumnSeparator.SPACE => ' '
      case ColumnSeparator.TAB => '\t'
      case ColumnSeparator.CUSTOM => csvCustomColumnSeparatorParameter.value(0)
    }
  }
}

object CsvParameters {

  object ColumnSeparator extends Enumeration {
    type ColumnSeparator = Value
    val COMMA = Value("Comma")
    val SEMICOLON = Value("Semicolon")
    val COLON = Value("Colon")
    val SPACE = Value("Space")
    val TAB = Value("Tab")
    val CUSTOM = Value("Custom column separator")
  }
}
