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

package ai.deepsense.deeplang.doperables.multicolumn

import ai.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.MultiColumnInPlaceChoices.MultiColumnYesInPlace
import ai.deepsense.deeplang.doperables.multicolumn.SingleColumnParams.SingleColumnInPlaceChoice
import ai.deepsense.deeplang.params._
import ai.deepsense.deeplang.params.choice.{Choice, ChoiceParam}
import ai.deepsense.deeplang.params.selections.{MultipleColumnSelection, NameColumnSelection, SingleColumnSelection}

object MultiColumnParams {

  sealed abstract class MultiColumnInPlaceChoice extends Choice {
    override val choiceOrder = MultiColumnInPlaceChoices.choiceOrder
  }

  object MultiColumnInPlaceChoices {
    val choiceOrder: List[Class[_ <: Choice]] =
      List(classOf[MultiColumnYesInPlace], classOf[MultiColumnNoInPlace])

    case class MultiColumnYesInPlace() extends MultiColumnInPlaceChoice {
      override val name: String = "replace input columns"
      override val params: Array[Param[_]] = Array()
    }
    case class MultiColumnNoInPlace() extends MultiColumnInPlaceChoice {
      override val name: String = "append new columns"

      val outputColumnsPrefixParam = PrefixBasedColumnCreatorParam(
        name = "column name prefix",
        description = Some("Prefix for output columns.")
      )

      override val params: Array[Param[_]] = Array(outputColumnsPrefixParam)

      def getColumnsPrefix: String = $(outputColumnsPrefixParam)
      def setColumnsPrefix(prefix: String): this.type = set(outputColumnsPrefixParam, prefix)
    }
  }

  sealed abstract class SingleOrMultiColumnChoice extends Choice {
    override val choiceOrder = SingleOrMultiColumnChoices.choiceOrder
  }

  object SingleOrMultiColumnChoices {
    val choiceOrder: List[Class[_ <: Choice]] =
      List(classOf[SingleColumnChoice], classOf[MultiColumnChoice])

    case class SingleColumnChoice() extends SingleOrMultiColumnChoice with HasSingleInPlaceParam {
      override val name: String = "one column"

      val inputColumn = SingleColumnSelectorParam(
        name = "input column",
        description = Some("Column to transform."),
        portIndex = 0
      )

      override val params: Array[Param[_]] =
        Array(inputColumn, singleInPlaceChoice)

      def setInputColumn(value: SingleColumnSelection): this.type = set(inputColumn, value)
      def setInPlace(value: SingleColumnInPlaceChoice): this.type =
        set(singleInPlaceChoice, value)
      def getInputColumn: SingleColumnSelection = $(inputColumn)
      def getInPlace: SingleColumnInPlaceChoice = $(singleInPlaceChoice)
    }

    case class MultiColumnChoice() extends SingleOrMultiColumnChoice {
      override val name: String = "multiple columns"

      val inputColumnsParam = ColumnSelectorParam(
        name = "input columns",
        description = Some("Columns to transform."),
        portIndex = 0
      )

      val multiInPlaceChoiceParam = ChoiceParam[MultiColumnInPlaceChoice](
        name = "output",
        description = Some("Output generation mode.")
      )
      setDefault(multiInPlaceChoiceParam, MultiColumnYesInPlace())

      override val params: Array[Param[_]] = Array(
        inputColumnsParam,
        multiInPlaceChoiceParam
      )

      def getMultiInputColumnSelection: MultipleColumnSelection = $(inputColumnsParam)
      def getMultiInPlaceChoice: MultiColumnInPlaceChoice = $(multiInPlaceChoiceParam)
      def setInputColumnsParam(value: MultipleColumnSelection): this.type =
        set(inputColumnsParam, value)
      def setInputColumnsParam(inputColumnNames: Set[String]): this.type =
        setInputColumnsParam(MultipleColumnSelection(Vector(NameColumnSelection(inputColumnNames))))
      def setMultiInPlaceChoice(value: MultiColumnInPlaceChoice): this.type =
        set(multiInPlaceChoiceParam, value)
    }

    object MultiColumnChoice {
      def apply(inputColumnNames: Set[String]): MultiColumnChoice = {
        MultiColumnChoice().setInputColumnsParam(inputColumnNames)
      }
    }
  }
}
