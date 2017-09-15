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

package io.deepsense.deeplang.doperations

import scala.collection.immutable.ListMap

import org.apache.spark.mllib.tree.{RandomForest => SparkRandomForest}
import io.deepsense.deeplang.doperables.machinelearning.randomforest.RandomForestParameters
import io.deepsense.deeplang.parameters.{ChoiceParameter, NumericParameter, ParametersSchema, RangeValidator}

trait RandomForestParams {

  private val numTreesParameter = NumericParameter(
    description = "Number of trees in the random forest",
    default = Some(1.0),
    validator = RangeValidator(begin = 1.0, end = 1000, step = Some(1.0)))
  private val featureSubsetStrategyParameter = ChoiceParameter(
    description = "Number of features to consider for splits at each node",
    default = Some("auto"),
    options = ListMap(
      SparkRandomForest.supportedFeatureSubsetStrategies.toList.map(_ -> ParametersSchema()): _*))
  private val impurityParameter = ChoiceParameter(
    description = "Criterion used for information gain calculation",
    default = Some(impurityOptions(0)),
    options = ListMap(impurityOptions.map(_ -> ParametersSchema()): _*))
  private val maxDepthParameter = NumericParameter(
    description = "Maximum depth of the tree",
    default = Some(4.0),
    validator = RangeValidator(begin = 1.0, end = 1000, step = Some(1.0)))
  private val maxBinsParameter = NumericParameter(
    description = "Maximum number of bins used for splitting features",
    default = Some(100.0),
    validator = RangeValidator(begin = 1.0, end = 100000, step = Some(1.0)))

  val impurityOptions: Seq[String]

  val parameters = ParametersSchema(
    "num trees" -> numTreesParameter,
    "feature subset strategy" -> featureSubsetStrategyParameter,
    "impurity" -> impurityParameter,
    "max depth" -> maxDepthParameter,
    "max bins" -> maxBinsParameter
  )

  def setParameters(
      numTrees: Int,
      featureSubsetStrategy: String,
      impurity: String,
      maxDepth: Int,
      maxBins: Int): Unit = {
    numTreesParameter.value = numTrees
    featureSubsetStrategyParameter.value = featureSubsetStrategy
    impurityParameter.value = impurity
    maxDepthParameter.value = maxDepth
    maxBinsParameter.value = maxBins
  }

  def modelParameters: RandomForestParameters = {
    val numTrees = numTreesParameter.value
    val featureSubsetStrategy = featureSubsetStrategyParameter.value
    val impurity = impurityParameter.value
    val maxDepth = maxDepthParameter.value
    val maxBins = maxBinsParameter.value

    RandomForestParameters(
      numTrees.toInt, featureSubsetStrategy, impurity, maxDepth.toInt, maxBins.toInt)
  }
}
