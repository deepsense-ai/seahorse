/**
 * Copyright 2015, deepsense.ai
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

package io.deepsense.deeplang.doperables.spark.wrappers.params.common

import scala.language.reflectiveCalls

import org.apache.spark.ml

import io.deepsense.deeplang.params.Params
import io.deepsense.deeplang.params.validators.RangeValidator
import io.deepsense.deeplang.params.wrappers.spark.DoubleParamWrapper

trait HasStepSizeParam extends Params {

  lazy val stepSizeDefault = 0.1

  val stepSize = new DoubleParamWrapper[ml.param.Params { val stepSize: ml.param.DoubleParam }](
    name = "step size",
    description = Some("The step size to be used for each iteration of optimization."),
    sparkParamGetter = _.stepSize,
    validator = RangeValidator(begin = 0.0, end = Double.MaxValue))
  setDefault(stepSize, stepSizeDefault)
}
