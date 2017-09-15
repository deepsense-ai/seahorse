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

import io.deepsense.deeplang.doperables.machinelearning.svm.SupportVectorMachineParameters
import io.deepsense.deeplang.parameters.RegularizationType
import io.deepsense.deeplang.parameters.RegularizationType._
import io.deepsense.deeplang.parameters._

trait SupportVectorMachineInterfaceParameters {

  private val numberOfIterations = NumericParameter(
    description = "The number of iterations for SGD",
    default = Some(100.0),
    validator = RangeValidator(begin = 1.0, end = 1000000.0, step = Some(1.0)))

  private val regularizationParameterParameter = NumericParameter(
    description = "Regularization parameter used in loss function",
    default = Some(0.0),
    validator = RangeValidator(begin = 0.0, end = 100000.0))

  private val regularizationTypeParameter = ChoiceParameter(
    "What kind or regularization, if any, should be used?",
    Some(NONE.toString),
    options = ListMap(
      NONE.toString -> ParametersSchema(),
      L1.toString -> ParametersSchema(
        "regularization parameter" -> regularizationParameterParameter),
      L2.toString -> ParametersSchema(
        "regularization parameter" -> regularizationParameterParameter)))

  private val miniBatchFractionParameter = NumericParameter(
    description = "Fraction of data to be used for each SGD iteration",
    default = Some(1.0),
    validator = RangeValidator(begin = 0.0, end = 1.0, beginIncluded = false))

  def setParameters(
      regularization: RegularizationType,
      numIterations: Int,
      regParam: Double,
      miniBatchFraction: Double): Unit = {

    regularizationTypeParameter.value = Some(regularization.toString)
    numberOfIterations.value = Some(numIterations.toDouble)
    regularizationParameterParameter.value = Some(regParam)
    miniBatchFractionParameter.value = Some(miniBatchFraction)
  }

  val parameters = ParametersSchema(
    "number of iterations" -> numberOfIterations,
    "regularization" -> regularizationTypeParameter,
    "mini-batch fraction" -> miniBatchFractionParameter)

  def modelParameters: SupportVectorMachineParameters = {
    SupportVectorMachineParameters(
      RegularizationType.withName(regularizationTypeParameter.value),
      numberOfIterations.value.toInt,
      regularizationParameterParameter.value,
      miniBatchFractionParameter.value
    )
  }
}
