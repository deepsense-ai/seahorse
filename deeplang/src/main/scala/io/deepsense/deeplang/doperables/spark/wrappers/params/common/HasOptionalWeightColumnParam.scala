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

package io.deepsense.deeplang.doperables.spark.wrappers.params.common

import scala.language.reflectiveCalls

import org.apache.spark.ml
import org.apache.spark.ml.param.{Param => SparkParam}

import io.deepsense.deeplang.params.Params
import io.deepsense.deeplang.params.choice.{Choice, ChoiceParam}
import io.deepsense.deeplang.params.selections.{NameSingleColumnSelection, SingleColumnSelection}
import io.deepsense.deeplang.params.wrappers.spark.{ParamsWithSparkWrappers, SingleColumnSelectorParamWrapper}

trait HasOptionalWeightColumnParam extends Params {

  val optionalWeightColumn =
    new ChoiceParam[OptionalWeightColumnChoice.WeightColumnOption](
      name = "use custom weights",
      description =
        """Whether to over-/under-sample training instances according to the given weights in
          |the `weight column`. If the `weight column` is not specified,
          |all instances are treated equally with a weight 1.0.""".stripMargin)

  setDefault(optionalWeightColumn, OptionalWeightColumnChoice.WeightColumnNoOption())
}

object OptionalWeightColumnChoice {

  sealed trait WeightColumnOption
    extends Choice with ParamsWithSparkWrappers {
    override val choiceOrder: List[Class[_ <: WeightColumnOption]] = List(
      classOf[WeightColumnNoOption],
      classOf[WeightColumnYesOption])
  }

  case class WeightColumnYesOption() extends WeightColumnOption {
    val weightColumn = new SingleColumnSelectorParamWrapper[
      ml.param.Params { val weightCol: SparkParam[String]}](
      name = "weight column",
      description = "The weight column for a model.",
      sparkParamGetter = _.weightCol,
      portIndex = 0)
    setDefault(weightColumn, NameSingleColumnSelection("weight"))

    def getWeightColumn: SingleColumnSelection = $(weightColumn)
    def setWeightColumn(value: SingleColumnSelection): this.type = set(weightColumn -> value)

    override val name = "yes"
    override val params = declareParams(weightColumn)
  }

  case class WeightColumnNoOption() extends WeightColumnOption {
    override val name = "no"
    override val params = declareParams()
  }
}
