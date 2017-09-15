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

import io.deepsense.deeplang.parameters.{NumericParameter, ParametersSchema, RangeValidator}

trait LinearRegressionParams {
  val regularizationParameter = NumericParameter(
    description = "Regularization parameter",
    default = Some(0.0),
    validator = RangeValidator(begin = 0.0, end = Double.PositiveInfinity))

  val iterationsNumberParameter = NumericParameter(
    description = "Number of iterations to perform",
    default = Some(1.0),
    validator = RangeValidator(begin = 1.0, end = 1000000, step = Some(1.0)))

  val miniBatchFractionParameter = NumericParameter(
    description = "Mini batch fraction",
    default = Some(1.0),
    validator = RangeValidator(begin = 0.0, end = 1.0, beginIncluded = false))

  val parameters = ParametersSchema(
    "regularization" -> regularizationParameter,
    "iterations number" -> iterationsNumberParameter,
    "mini batch fraction" -> miniBatchFractionParameter)
}
