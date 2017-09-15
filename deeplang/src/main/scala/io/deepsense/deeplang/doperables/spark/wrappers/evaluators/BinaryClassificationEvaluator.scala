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

package io.deepsense.deeplang.doperables.spark.wrappers.evaluators

import org.apache.spark.ml.evaluation.{BinaryClassificationEvaluator => SparkBinaryClassificationEvaluator}

import io.deepsense.deeplang.doperables.SparkEvaluatorWrapper
import io.deepsense.deeplang.doperables.spark.wrappers.params.common.{HasLabelColumnParam, HasRawPredictionColumnParam}
import io.deepsense.deeplang.params.Param
import io.deepsense.deeplang.params.choice.Choice
import io.deepsense.deeplang.params.wrappers.spark.ChoiceParamWrapper

class BinaryClassificationEvaluator
  extends SparkEvaluatorWrapper[SparkBinaryClassificationEvaluator]
  with HasRawPredictionColumnParam
  with HasLabelColumnParam {

  import BinaryClassificationEvaluator._

  val metricName = new ChoiceParamWrapper[SparkBinaryClassificationEvaluator, Metric](
    name = "binary metric",
    description = "The metric used in evaluation.",
    sparkParamGetter = _.metricName)
  setDefault(metricName, AreaUnderROC())

  override val params: Array[Param[_]] = declareParams(metricName, rawPredictionColumn, labelColumn)

  override def getMetricName: String = $(metricName).name
}

object BinaryClassificationEvaluator {

  sealed abstract class Metric(override val name: String) extends Choice {

    override val params: Array[Param[_]] = declareParams()

    override val choiceOrder: List[Class[_ <: Choice]] = List(
      classOf[AreaUnderROC],
      classOf[AreaUnderPR]
    )
  }

  case class AreaUnderROC() extends Metric("areaUnderROC")

  case class AreaUnderPR() extends Metric("areaUnderPR")
}
