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
import io.deepsense.deeplang.doperables.machinelearning.gradientboostedtrees.GradientBoostedTreesParameters
import io.deepsense.deeplang.parameters.{ChoiceParameter, ParametersSchema, RangeValidator, NumericParameter}

trait GradientBoostedTreesParams {

  private val numIterationsParameter = NumericParameter(
    description = "Number of iterations",
    default = Some(100.0),
    validator = RangeValidator(begin = 1.0, end = 1000000.0, step = Some(1.0)))
  private val lossParameter = ChoiceParameter(
    description = "Loss function used for minimization during gradient boosting",
    default = Some(lossOptions(0)),
    options = ListMap(lossOptions.map(_ -> ParametersSchema()): _*))
  private val impurityParameter = ChoiceParameter(
    description = "Criterion used for information gain calculation",
    default = Some(impurityOptions(0)),
    options = ListMap(impurityOptions.map(_ -> ParametersSchema()): _*))
  private val maxDepthParameter = NumericParameter(
    description = "Maximum depth of the tree",
    default = Some(4.0),
    validator = RangeValidator(begin = 1.0, end = 100000, step = Some(1.0)))
  private val maxBinsParameter = NumericParameter(
    description = "Maximum number of bins used for splitting features",
    default = Some(32.0),
    validator = RangeValidator(begin = 1.0, end = 100000, step = Some(1.0)))

  val lossOptions: Seq[String]

  val impurityOptions: Seq[String]

  val parameters = ParametersSchema(
    "num iterations" -> numIterationsParameter,
    "loss" -> lossParameter,
    "impurity" -> impurityParameter,
    "max depth" -> maxDepthParameter,
    "max bins" -> maxBinsParameter
  )

  def setParameters(numIterations: Int,
                    loss: String,
                    impurity: String,
                    maxDepth: Int,
                    maxBins: Int): Unit = {
    numIterationsParameter.value = numIterations
    lossParameter.value = loss
    impurityParameter.value = impurity
    maxDepthParameter.value = maxDepth
    maxBinsParameter.value = maxBins
  }

  def modelParameters: GradientBoostedTreesParameters = {
    val numIterations = numIterationsParameter.value
    val loss = lossParameter.value
    val impurity = impurityParameter.value
    val maxDepth = maxDepthParameter.value
    val maxBins = maxBinsParameter.value

    GradientBoostedTreesParameters(
      numIterations.toInt, loss, impurity, maxDepth.toInt, maxBins.toInt)
  }
}
