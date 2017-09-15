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

package io.deepsense.deeplang.doperables.spark.wrappers.estimators

import scala.language.reflectiveCalls

import org.apache.spark.ml
import org.apache.spark.ml.regression.{GBTRegressionModel => SparkGBTRegressionModel, GBTRegressor => SparkGBTRegressor}

import io.deepsense.deeplang.doperables.SparkEstimatorWrapper
import io.deepsense.deeplang.doperables.spark.wrappers.models.GBTRegressionModel
import io.deepsense.deeplang.doperables.spark.wrappers.params.GBTParams
import io.deepsense.deeplang.params.Param
import io.deepsense.deeplang.params.choice.Choice
import io.deepsense.deeplang.params.wrappers.spark.ChoiceParamWrapper

class GBTRegressor
  extends SparkEstimatorWrapper[
    SparkGBTRegressionModel,
    SparkGBTRegressor,
    GBTRegressionModel]
  with GBTParams {

  import GBTRegressor._

  override lazy val maxIterationsDefault = 20.0

  val impurity = new ChoiceParamWrapper[
    ml.param.Params { val impurity: ml.param.Param[String] }, Impurity](
    name = "impurity",
    description = "The criterion used for information gain calculation.",
    sparkParamGetter = _.impurity)
  setDefault(impurity, Variance())

  val lossType = new ChoiceParamWrapper[
    ml.param.Params { val lossType: ml.param.Param[String] }, LossType](
    name = "loss function",
    description = "The loss function which GBT tries to minimize.",
    sparkParamGetter = _.lossType)
  setDefault(lossType, Squared())

  override val params: Array[Param[_]] = declareParams(
    featuresColumn,
    impurity,
    labelColumn,
    lossType,
    maxBins,
    maxDepth,
    maxIterations,
    minInfoGain,
    minInstancesPerNode,
    predictionColumn,
    seed,
    stepSize,
    subsamplingRate)
}

object GBTRegressor {

  sealed abstract class Impurity(override val name: String) extends Choice {
    override val params: Array[Param[_]] = declareParams()
    override val choiceOrder: List[Class[_ <: Choice]] = List(
      classOf[Variance]
    )
  }
  case class Variance() extends Impurity("variance")

  sealed abstract class LossType(override val name: String) extends Choice {
    override val params: Array[Param[_]] = declareParams()

    override val choiceOrder: List[Class[_ <: Choice]] = List(
      classOf[Squared],
      classOf[Absolute]
    )
  }
  case class Squared() extends LossType("squared")
  case class Absolute() extends LossType("absolute")

}
