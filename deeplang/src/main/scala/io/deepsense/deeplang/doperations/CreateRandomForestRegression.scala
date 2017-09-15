/**
 * Copyright 2015, CodiLime Inc.
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
import scala.reflect.runtime.{universe => ru}

import org.apache.spark.mllib.tree.{RandomForest => SparkRandomForest}

import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang._
import io.deepsense.deeplang.doperables.{UntrainedRandomForestModel, UntrainedRandomForestRegression}
import io.deepsense.deeplang.parameters._

case class CreateRandomForestRegression() extends DOperation0To1[UntrainedRandomForestRegression] {

  override val name = "Random Forest Regression"

  override val id: Id = "0496cdad-32b4-4231-aec4-4e7e312c9e4e"

  private val numTreesParameter = NumericParameter(
    description = "Number of trees in the random forest",
    default = Some(1.0),
    required = true,
    validator = RangeValidator(begin = 1.0, end = 1000, step = Some(1.0)))
  private val featureSubsetStrategyParameter = ChoiceParameter(
    description = "Number of features to consider for splits at each node",
    default = Some("auto"),
    required = true,
    options = ListMap(
      SparkRandomForest.supportedFeatureSubsetStrategies.toList.map(_ -> ParametersSchema()): _*))
  private val impurityParameter = ChoiceParameter(
    description = "Criterion used for information gain calculation",
    default = Some("variance"),
    required = true,
    options = ListMap("variance" -> ParametersSchema()))
  private val maxDepthParameter = NumericParameter(
    description = "Maximum depth of the tree",
    default = Some(4.0),
    required = true,
    validator = RangeValidator(begin = 1.0, end = 1000, step = Some(1.0)))
  private val maxBinsParameter = NumericParameter(
    description = "Maximum number of bins used for splitting features",
    default = Some(100.0),
    required = true,
    validator = RangeValidator(begin = 1.0, end = 100000, step = Some(1.0)))

  override val parameters = ParametersSchema(
    "num trees" -> numTreesParameter,
    "feature subset strategy" -> featureSubsetStrategyParameter,
    "impurity" -> impurityParameter,
    "max depth" -> maxDepthParameter,
    "max bins" -> maxBinsParameter
  )

  override protected def _execute(context: ExecutionContext)(): UntrainedRandomForestRegression = {
    val numTrees = numTreesParameter.value.get
    val featureSubsetStrategy = featureSubsetStrategyParameter.value.get
    val impurity = impurityParameter.value.get
    val maxDepth = maxDepthParameter.value.get
    val maxBins = maxBinsParameter.value.get

    val model = UntrainedRandomForestModel(numTrees.toInt, featureSubsetStrategy, impurity,
      maxDepth.toInt, maxBins.toInt)

    UntrainedRandomForestRegression(model)
  }

  override val tTagTO_0: ru.TypeTag[UntrainedRandomForestRegression] =
    ru.typeTag[UntrainedRandomForestRegression]
}

object CreateRandomForestRegression {

  def apply(numTrees: Int, featureSubsetStrategy: String, impurity: String,
      maxDepth: Int, maxBins: Int): CreateRandomForestRegression = {
    val createRandomForest = CreateRandomForestRegression()
    createRandomForest.numTreesParameter.value = Some(numTrees)
    createRandomForest.featureSubsetStrategyParameter.value = Some(featureSubsetStrategy)
    createRandomForest.impurityParameter.value = Some(impurity)
    createRandomForest.maxDepthParameter.value = Some(maxDepth)
    createRandomForest.maxBinsParameter.value = Some(maxBins)
    createRandomForest
  }
}
