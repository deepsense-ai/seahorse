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

import org.apache.spark.ml.classification.{GBTClassificationModel => SparkGBTClassificationModel, GBTClassifier => SparkGBTClassifier}

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.spark.wrappers.models.GBTClassificationModel
import io.deepsense.deeplang.doperables.spark.wrappers.params.GBTParams
import io.deepsense.deeplang.params.Param
import io.deepsense.deeplang.params.choice.Choice
import io.deepsense.deeplang.params.wrappers.spark.ChoiceParamWrapper
import io.deepsense.deeplang.utils.WithStringIndexing

class GBTClassifier()
  extends SparkEstimatorWrapper[
    SparkGBTClassificationModel,
    SparkGBTClassifier,
    GBTClassificationModel]
  with GBTParams
  with Logging
  with WithStringIndexing[
    SparkGBTClassificationModel,
    SparkGBTClassifier,
    GBTClassificationModel] {

  import GBTClassifier._

  setDefault(maxIterations, 20.0)

  setDefault(stepSize, 0.1)

  override private[deeplang] def _fit(ctx: ExecutionContext, dataFrame: DataFrame): Transformer = {
    val labelColumnName = dataFrame.getColumnName($(labelColumn))
    val predictionColumnName: String = $(predictionColumn)
    fitWithStringIndexing(ctx, dataFrame, this, labelColumnName, predictionColumnName)
  }

  val impurity = new ChoiceParamWrapper[SparkGBTClassifier, Impurity](
    name = "impurity",
    description = "Criterion used for information gain calculation",
    sparkParamGetter = _.impurity)
  setDefault(impurity, Gini())

  val lossType = new ChoiceParamWrapper[SparkGBTClassifier, LossType](
    name = "loss function",
    description = "Loss function which GBT tries to minimize",
    sparkParamGetter = _.lossType)
  setDefault(lossType, Logistic())

  override def report(executionContext: ExecutionContext): Report = Report()

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

object GBTClassifier {

  sealed abstract class Impurity(override val name: String) extends Choice {

    override val params: Array[Param[_]] = declareParams()

    override val choiceOrder: List[Class[_ <: Choice]] = List(
      classOf[Entropy],
      classOf[Gini]
    )
  }
  case class Entropy() extends Impurity("entropy")
  case class Gini() extends Impurity("gini")

  sealed abstract class LossType(override val name: String) extends Choice {

    override val params: Array[Param[_]] = declareParams()

    override val choiceOrder: List[Class[_ <: Choice]] = List(
      classOf[Logistic]
    )
  }
  case class Logistic() extends LossType("logistic")

}
