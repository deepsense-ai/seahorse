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

import org.apache.spark.ml.regression.{RandomForestRegressor => SparkRFR, RandomForestRegressionModel => SparkRFRModel}

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.spark.wrappers.models.RandomForestRegressionModel
import io.deepsense.deeplang.doperables.spark.wrappers.params.common._
import io.deepsense.deeplang.doperables.{Report, SparkEstimatorWrapper}
import io.deepsense.deeplang.params.Param
import io.deepsense.deeplang.params.choice.Choice
import io.deepsense.deeplang.params.validators.RangeValidator
import io.deepsense.deeplang.params.wrappers.spark._

class RandomForestRegression
  extends SparkEstimatorWrapper[SparkRFRModel, SparkRFR, RandomForestRegressionModel]
  with PredictorParams
  with HasLabelColumnParam
  with HasSeedParam
  with Logging {

  val maxDepth = new IntParamWrapper[SparkRFR](
    name = "max depth",
    description = "Maximum depth of each tree in the forest (>= 0)",
    sparkParamGetter = _.maxDepth,
    validator = RangeValidator.positiveIntegers)
  setDefault(maxDepth, 5.0)

  val maxBins = new IntParamWrapper[SparkRFR](
    name = "max bins",
    description = "Maximum number of bins discretizing continuous features (>= 2 and >= number " +
      "of categories for any categorical feature)",
    sparkParamGetter = _.maxBins,
    validator = RangeValidator(begin = 2.0, end = Int.MaxValue, step = Some(1.0)))
  setDefault(maxBins, 32.0)

  val minInstancesPerNode = new IntParamWrapper[SparkRFR](
    name = "min instances per node",
    description = "Minimum number of instances each child must have after split (>= 1)",
    sparkParamGetter = _.minInstancesPerNode,
    validator = RangeValidator(begin = 1.0, end = Int.MaxValue, step = Some(1.0)))
  setDefault(minInstancesPerNode, 1.0)

  val minInfoGain = new DoubleParamWrapper[SparkRFR](
    name = "min info gain",
    description = "Minimum information gain for a split to be considered at a tree node (>= 0)",
    sparkParamGetter = _.minInfoGain,
    validator = RangeValidator(0.0, Double.PositiveInfinity))
  setDefault(minInfoGain, 0.0)

  val maxMemoryInMB = new IntParamWrapper[SparkRFR](
    name = "max memory",
    description = "Maximum memory in MB allocated to histogram aggregation (>= 0)",
    sparkParamGetter = _.maxMemoryInMB,
    validator = RangeValidator.positiveIntegers)
  setDefault(maxMemoryInMB, 256.0)

  val cacheNodeIds = new BooleanParamWrapper[SparkRFR](
    name = "cache node ids",
    description = "Caching nodes IDs. Can speed up training of deeper trees.",
    sparkParamGetter = _.cacheNodeIds)
  setDefault(cacheNodeIds, false)

  val checkpointInterval = new IntParamWrapper[SparkRFR](
    name = "checkpoint interval",
    description = "Specifies how often to checkpoint the cached node IDs in intervals (>= 1)",
    sparkParamGetter = _.checkpointInterval,
    validator = RangeValidator(begin = 1.0, end = Int.MaxValue, step = Some(1.0)))
  setDefault(checkpointInterval, 10.0)

  val impurity = new ChoiceParamWrapper[SparkRFR, Impurity.Criterion](
    name = "impurity",
    description = "Criterion used for information gain calculation",
    sparkParamGetter = _.impurity)
  setDefault(impurity, Impurity.Variance())

  val subsamplingRate = new DoubleParamWrapper[SparkRFR](
    name = "subsampling rate",
    description = "Fraction of the training data used for learning each decision tree, " +
      "in range (0, 1]",
    sparkParamGetter = _.subsamplingRate,
    validator = RangeValidator(begin = 0.0, end = 1.0, beginIncluded = false))
  setDefault(subsamplingRate, 1.0)

  val numTrees = new IntParamWrapper[SparkRFR](
    name = "num trees",
    description = "Number of trees to train (>= 1)",
    sparkParamGetter = _.numTrees,
    validator = RangeValidator(begin = 1.0, end = Int.MaxValue, step = Some(1.0)))
  setDefault(numTrees, 20.0)

  val featureSubsetStrategy = new ChoiceParamWrapper[SparkRFR, FeatureSubsetStrategy.Option](
    name = "feature subset strategy",
    description = "The number of features to consider for splits at each tree node",
    sparkParamGetter = _.featureSubsetStrategy)
  setDefault(featureSubsetStrategy, FeatureSubsetStrategy.Auto())

  override def report(executionContext: ExecutionContext): Report = Report()

  override val params: Array[Param[_]] = declareParams(
    maxDepth,
    maxBins,
    minInstancesPerNode,
    minInfoGain,
    maxMemoryInMB,
    cacheNodeIds,
    checkpointInterval,
    impurity,
    subsamplingRate,
    seed,
    numTrees,
    featureSubsetStrategy,
    featuresColumn,
    predictionColumn,
    labelColumn
  )
}

object Impurity {

  sealed abstract class Criterion(override val name: String) extends Choice {

    override val params: Array[Param[_]] = declareParams()

    override val choiceOrder: List[Class[_ <: Choice]] = List(
      classOf[Variance]
    )
  }

  case class Variance() extends Criterion("variance")
}


object FeatureSubsetStrategy {

  sealed abstract class Option(override val name: String) extends Choice {

    override val params: Array[Param[_]] = declareParams()

    override val choiceOrder: List[Class[_ <: Choice]] = List(
      classOf[Auto],
      classOf[OneThird],
      classOf[Sqrt],
      classOf[Log2]
    )
  }

  case class Auto() extends Option("auto")
  case class OneThird() extends Option("onethird")
  case class Sqrt() extends Option("sqrt")
  case class Log2() extends Option("log2")
}
