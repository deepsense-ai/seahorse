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

package ai.deepsense.deeplang.doperables.spark.wrappers.evaluators

import org.apache.spark.ml.evaluation.{MulticlassClassificationEvaluator => SparkMulticlassClassificationEvaluator}

import ai.deepsense.deeplang.doperables.SparkEvaluatorWrapper
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.{HasLabelColumnParam, HasPredictionColumnSelectorParam}
import ai.deepsense.deeplang.params.Param
import ai.deepsense.deeplang.params.choice.Choice
import ai.deepsense.deeplang.params.wrappers.spark.ChoiceParamWrapper

class MulticlassClassificationEvaluator
  extends SparkEvaluatorWrapper[SparkMulticlassClassificationEvaluator]
  with HasPredictionColumnSelectorParam
  with HasLabelColumnParam {

  import MulticlassClassificationEvaluator._

  val metricName = new ChoiceParamWrapper[SparkMulticlassClassificationEvaluator, Metric](
    name = "multiclass metric",
    description = Some("The metric used in evaluation."),
    sparkParamGetter = _.metricName)
  setDefault(metricName, F1())

  override val params: Array[Param[_]] = Array(metricName, predictionColumn, labelColumn)

  override def getMetricName: String = $(metricName).name
}

object MulticlassClassificationEvaluator {

  sealed abstract class Metric(override val name: String) extends Choice {

    override val params: Array[Param[_]] = Array()

    override val choiceOrder: List[Class[_ <: Choice]] = List(
      classOf[F1],
      classOf[Precision],
      classOf[Recall],
      classOf[WeightedPrecision],
      classOf[WeightedRecall]
    )
  }

  case class F1() extends Metric("f1")

  case class Precision() extends Metric("precision")

  case class Recall() extends Metric("recall")

  case class WeightedPrecision() extends Metric("weightedPrecision")

  case class WeightedRecall() extends Metric("weightedRecall")
}
