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

import org.apache.spark.ml
import org.apache.spark.ml.classification.{GBTClassificationModel => SparkGBTClassificationModel, GBTClassifier => SparkGBTClassifier}

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.spark.wrappers.estimators.GBTClassifier.{Gini, Impurity, Logistic, LossType}
import io.deepsense.deeplang.doperables.spark.wrappers.models.GBTClassificationModel
import io.deepsense.deeplang.doperables.spark.wrappers.params.common._
import io.deepsense.deeplang.params.Param
import io.deepsense.deeplang.params.choice.Choice
import io.deepsense.deeplang.params.validators.RangeValidator
import io.deepsense.deeplang.params.wrappers.spark.{ChoiceParamWrapper, DoubleParamWrapper, IntParamWrapper}
import io.deepsense.deeplang.utils.WithStringIndexing

class GBTClassifier()
  extends SparkEstimatorWrapper[
    SparkGBTClassificationModel,
    SparkGBTClassifier,
    GBTClassificationModel]
  with PredictorParams
  with HasLabelColumnParam
  with HasMaxIterationsParam
  with HasSeedParam
  with HasStepSizeParam
  with Logging
  with WithStringIndexing[
    SparkGBTClassificationModel,
    SparkGBTClassifier,
    GBTClassificationModel] {

  override val maxIterationsDefault: Double = 20.0
  override val stepSizeDefault: Double = 0.1

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

  val maxBins = new IntParamWrapper[ml.param.Params { val maxBins: ml.param.IntParam }](
    name = "max bins",
    description = "Maximum number of bins used for discretizing continuous features " +
      "and for choosing how to split on features at each node. " +
      "More bins give higher granularity. " +
      "Must be >= 2 and >= number of categories in any categorical feature.",
    sparkParamGetter = _.maxBins,
    RangeValidator(2.0, Int.MaxValue, step = Some(1.0)))
  setDefault(maxBins, 32.0)

  val maxDepth = new IntParamWrapper[ml.param.Params { val maxDepth: ml.param.IntParam }](
    name = "max depth",
    description = "Maximum depth of the tree (>= 0). " +
      "E.g. depth 0 means 1 leaf node; depth 1 means 1 internal node + 2 leaf nodes.",
    sparkParamGetter = _.maxDepth,
    RangeValidator.positiveIntegers)
  setDefault(maxDepth, 5.0)

  val minInfoGain =
    new DoubleParamWrapper[ml.param.Params { val minInfoGain: ml.param.DoubleParam }](
      name = "min information gain",
      description = "Minimum information gain for a split to be considered at a tree node",
      sparkParamGetter = _.minInfoGain,
      RangeValidator(0.0, Double.MaxValue))
  setDefault(minInfoGain, 0.0)

  val minInstancesPerNode =
    new IntParamWrapper[ml.param.Params { val minInstancesPerNode: ml.param.IntParam }](
      name = "min instances per node",
      description = "Minimum number of instances each child must have after split. " +
        "If a split causes the left or right child to have fewer than minInstancesPerNode, " +
        "the split will be discarded as invalid. Should be >= 1.",
      sparkParamGetter = _.minInstancesPerNode,
      RangeValidator(1.0, Int.MaxValue, step = Some(1.0)))
  setDefault(minInstancesPerNode, 1.0)

  val subsamplingRate =
    new DoubleParamWrapper[ml.param.Params { val subsamplingRate: ml.param.DoubleParam }](
      name = "subsampling rate",
      description =
        "Fraction of the training data used for learning each decision tree, in range (0, 1]",
      sparkParamGetter = _.subsamplingRate,
      RangeValidator(0.0, 1.0, beginIncluded = false))
  setDefault(subsamplingRate, 1.0)

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
