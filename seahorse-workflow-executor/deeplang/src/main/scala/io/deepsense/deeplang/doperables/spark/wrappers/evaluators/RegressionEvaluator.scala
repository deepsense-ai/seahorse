/**
 * Copyright 2015, deepsense.ai
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

import org.apache.spark.ml.evaluation.{RegressionEvaluator => SparkRegressionEvaluator}

import io.deepsense.deeplang.doperables.SparkEvaluatorWrapper
import io.deepsense.deeplang.doperables.spark.wrappers.params.common.{HasLabelColumnParam, HasPredictionColumnSelectorParam}
import io.deepsense.deeplang.params.Param
import io.deepsense.deeplang.params.choice.Choice
import io.deepsense.deeplang.params.wrappers.spark.ChoiceParamWrapper

class RegressionEvaluator
  extends SparkEvaluatorWrapper[SparkRegressionEvaluator]
  with HasPredictionColumnSelectorParam
  with HasLabelColumnParam {

  import RegressionEvaluator._

  val metricName = new ChoiceParamWrapper[SparkRegressionEvaluator, Metric](
    name = "regression metric",
    description = Some("The metric used in evaluation."),
    sparkParamGetter = _.metricName)
  setDefault(metricName, Rmse())

  override val params: Array[Param[_]] = Array(metricName, predictionColumn, labelColumn)

  override def getMetricName: String = $(metricName).name
}

object RegressionEvaluator {

  sealed abstract class Metric(override val name: String) extends Choice {

    override val params: Array[Param[_]] = Array()

    override val choiceOrder: List[Class[_ <: Choice]] = List(
      classOf[Mse],
      classOf[Rmse],
      classOf[R2],
      classOf[Mae]
    )
  }

  case class Mse() extends Metric("mse")

  case class Rmse() extends Metric("rmse")

  case class R2() extends Metric("r2")

  case class Mae() extends Metric("mae")
}
