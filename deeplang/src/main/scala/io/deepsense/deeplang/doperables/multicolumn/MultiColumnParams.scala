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

package io.deepsense.deeplang.doperables.multicolumn

import io.deepsense.deeplang.doperables.multicolumn.SingleColumnParams.SingleColumnInPlaceChoice
import io.deepsense.deeplang.params._
import io.deepsense.deeplang.params.choice.{Choice, ChoiceParam}
import io.deepsense.deeplang.params.selections.{MultipleColumnSelection, SingleColumnSelection}

object MultiColumnParams {

  sealed abstract class MultiColumnInPlaceChoice extends Choice {
    override val choiceOrder = MultiColumnInPlaceChoices.choiceOrder
  }

  object MultiColumnInPlaceChoices {
    val choiceOrder: List[Class[_ <: Choice]] =
      List(classOf[MultiColumnYesInPlace], classOf[MultiColumnNoInPlace])

    case class MultiColumnYesInPlace() extends MultiColumnInPlaceChoice {
      override val name: String = "Transform in place"
      override val params: Array[Param[_]] = declareParams()
    }
    case class MultiColumnNoInPlace() extends MultiColumnInPlaceChoice {
      override val name: String = "Append columns"

      val outputColumnsPrefixParam = PrefixBasedColumnCreatorParam(
        name = "column name prefix",
        description = "Prefix for output columns"
      )

      override val params: Array[Param[_]] = declareParams(outputColumnsPrefixParam)

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

    case class SingleColumnChoice() extends SingleOrMultiColumnChoice {
      override val name: String = "Transform one column"

      val inputColumn = SingleColumnSelectorParam(
        name = "input column",
        description = "A column to be transformed",
        portIndex = 0
      )

      val singleInPlaceChoice = ChoiceParam[SingleColumnInPlaceChoice](
        name = "transform in place",
        description = "Should the transformation be done in place?"
      )

      override val params: Array[Param[_]] =
        declareParams(inputColumn, singleInPlaceChoice)

      def setInputColumn(value: SingleColumnSelection): this.type = set(inputColumn, value)
      def setInPlace(value: SingleColumnInPlaceChoice): this.type =
        set(singleInPlaceChoice, value)
      def getInputColumn: SingleColumnSelection = $(inputColumn)
      def getInPlace: SingleColumnInPlaceChoice = $(singleInPlaceChoice)
    }

    case class MultiColumnChoice() extends SingleOrMultiColumnChoice {
      override val name: String = "Transform multiple columns"

      val inputColumnsParam = ColumnSelectorParam(
        name = "input columns",
        description = "Columns to transform",
        portIndex = 0
      )

      val multiInPlaceChoiceParam = ChoiceParam[MultiColumnInPlaceChoice](
        name = "transform in place",
        description = "Should the transformation be done in place?"
      )

      override val params: Array[Param[_]] = declareParams(
        inputColumnsParam,
        multiInPlaceChoiceParam
      )

      def getMultiInputColumnSelection: MultipleColumnSelection = $(inputColumnsParam)
      def getMultiInPlaceChoice: MultiColumnInPlaceChoice = $(multiInPlaceChoiceParam)
      def setInputColumnsParam(value: MultipleColumnSelection): this.type =
        set(inputColumnsParam, value)
      def setMultiInPlaceChoice(value: MultiColumnInPlaceChoice): this.type =
        set(multiInPlaceChoiceParam, value)
    }
  }
}
