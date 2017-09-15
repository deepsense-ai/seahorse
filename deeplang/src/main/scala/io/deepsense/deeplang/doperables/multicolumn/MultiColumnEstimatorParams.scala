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

import io.deepsense.deeplang.params._
import io.deepsense.deeplang.params.choice.Choice
import io.deepsense.deeplang.params.selections.{MultipleColumnSelection, SingleColumnSelection}

object MultiColumnEstimatorParams {

  sealed abstract class SingleOrMultiColumnEstimatorChoice extends Choice {
    override val choiceOrder = SingleOrMultiColumnEstimatorChoices.choiceOrder
  }

  object SingleOrMultiColumnEstimatorChoices {

    val choiceOrder: List[Class[_ <: Choice]] =
      List(classOf[SingleColumnEstimatorChoice],
        classOf[MultiColumnEstimatorChoice])

    case class SingleColumnEstimatorChoice() extends SingleOrMultiColumnEstimatorChoice {
      override val name: String = "Fit to one column"

      val inputColumn = SingleColumnSelectorParam(
        name = "input column",
        description = "A column to be transformed",
        portIndex = 0
      )

      val outputColumnCreatorParam = SingleColumnCreatorParam(
        name = "output column",
        description = "column to save results"
      )

      def setOutputColumn(outputColumnName: String): this. type = {
        set(outputColumnCreatorParam -> outputColumnName)
      }

      def setInputColumn(inputColumnSelection: SingleColumnSelection): this.type = {
        set(inputColumn -> inputColumnSelection)
      }

      def getInputColumn: SingleColumnSelection = $(inputColumn)
      def getOutputColumn: String = $(outputColumnCreatorParam)


      override val params: Array[Param[_]] =
        declareParams(inputColumn, outputColumnCreatorParam)
    }

    case class MultiColumnEstimatorChoice() extends SingleOrMultiColumnEstimatorChoice {
      override val name: String = "Fit to many columns"

      val inputColumnsParam = ColumnSelectorParam(
        name = "input columns",
        description = "Columns to transform",
        portIndex = 0
      )

      val outputColumnsPrefixParam = PrefixBasedColumnCreatorParam(
        name = "column name prefix",
        description = "Prefix for output columns"
      )

      def setInputColumnsParam(value: MultipleColumnSelection): this.type =
        set(inputColumnsParam, value)

      def setOutputColumnsPrefix(value: String): this.type = {
        set(outputColumnsPrefixParam -> value)
      }

      def getInputColumns: MultipleColumnSelection = $(inputColumnsParam)
      def getOutputColumnsPrefix: String = $(outputColumnsPrefixParam)

      override val params: Array[Param[_]] =
        declareParams(inputColumnsParam, outputColumnsPrefixParam)
    }
  }
}
