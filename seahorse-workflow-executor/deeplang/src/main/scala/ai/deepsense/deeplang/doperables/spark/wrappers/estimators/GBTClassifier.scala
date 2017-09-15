/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperables.spark.wrappers.estimators

import scala.reflect.runtime.universe._

import org.apache.spark.ml.classification.{GBTClassificationModel => SparkGBTClassificationModel, GBTClassifier => SparkGBTClassifier}

import ai.deepsense.commons.utils.Logging
import ai.deepsense.deeplang.TypeUtils
import ai.deepsense.deeplang.doperables.SparkEstimatorWrapper
import ai.deepsense.deeplang.doperables.spark.wrappers.models.{GBTClassificationModel, VanillaGBTClassificationModel}
import ai.deepsense.deeplang.doperables.spark.wrappers.params.GBTParams
import ai.deepsense.deeplang.doperables.spark.wrappers.params.common.HasClassificationImpurityParam
import ai.deepsense.deeplang.doperables.stringindexingwrapper.StringIndexingEstimatorWrapper
import ai.deepsense.deeplang.params.Param
import ai.deepsense.deeplang.params.choice.Choice
import ai.deepsense.deeplang.params.wrappers.spark.ChoiceParamWrapper

class GBTClassifier private (val vanillaGBTClassifier: VanillaGBTClassifier)
  extends StringIndexingEstimatorWrapper[
    SparkGBTClassificationModel,
    SparkGBTClassifier,
    VanillaGBTClassificationModel,
    GBTClassificationModel](vanillaGBTClassifier) {

  def this() = this(new VanillaGBTClassifier())
}

class VanillaGBTClassifier()
  extends SparkEstimatorWrapper[
    SparkGBTClassificationModel,
    SparkGBTClassifier,
    VanillaGBTClassificationModel]
  with GBTParams
  with HasClassificationImpurityParam
  with Logging {

  import GBTClassifier._

  override lazy val maxIterationsDefault = 10.0

  val estimator = TypeUtils.instanceOfType(typeTag[SparkGBTClassifier])

  val lossType = new ChoiceParamWrapper[SparkGBTClassifier, LossType](
    name = "loss function",
    description = Some("The loss function which GBT tries to minimize."),
    sparkParamGetter = _.lossType)
  setDefault(lossType, Logistic())

  override val params: Array[Param[_]] = Array(
    impurity,
    lossType,
    maxBins,
    maxDepth,
    maxIterations,
    minInfoGain,
    minInstancesPerNode,
    seed,
    stepSize,
    subsamplingRate,
    labelColumn,
    featuresColumn,
    predictionColumn)

  override protected def estimatorName: String = classOf[GBTClassifier].getSimpleName
}

object GBTClassifier {

  sealed abstract class LossType(override val name: String) extends Choice {

    override val params: Array[Param[_]] = Array()

    override val choiceOrder: List[Class[_ <: Choice]] = List(
      classOf[Logistic]
    )
  }
  case class Logistic() extends LossType("logistic")

}
