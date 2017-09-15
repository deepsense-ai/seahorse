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

import ai.deepsense.deeplang.params.choice.Choice
import ai.deepsense.deeplang.params.{SingleColumnCreatorParam, Param, StringParam}

object SingleColumnParams {

  sealed abstract class SingleColumnInPlaceChoice extends Choice {
    override val choiceOrder = SingleTransformInPlaceChoices.choiceOrder
  }

  object SingleTransformInPlaceChoices {
    val choiceOrder: List[Class[_ <: Choice]] =
      List(classOf[YesInPlaceChoice], classOf[NoInPlaceChoice])

    case class YesInPlaceChoice() extends SingleColumnInPlaceChoice {
      override val name: String = "replace input column"
      override val params: Array[Param[_]] = Array()
    }

    case class NoInPlaceChoice() extends SingleColumnInPlaceChoice {
      val outputColumnCreatorParam = SingleColumnCreatorParam(
        name = "output column",
        description = Some("Column to save results to.")
      )
      override val name: String = "append new column"
      override val params: Array[Param[_]] = Array(outputColumnCreatorParam)

      def setOutputColumn(columnName: String): this.type = set(outputColumnCreatorParam, columnName)
      def getOutputColumn: String = $(outputColumnCreatorParam)
    }
  }
}
