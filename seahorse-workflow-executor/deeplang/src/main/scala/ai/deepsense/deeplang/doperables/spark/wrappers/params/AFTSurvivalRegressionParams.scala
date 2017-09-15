/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperables.spark.wrappers.params

import scala.language.reflectiveCalls

import org.apache.spark.ml

import ai.deepsense.deeplang.doperables.spark.wrappers.params.common._
import ai.deepsense.deeplang.params.Params
import ai.deepsense.deeplang.params.validators.{ArrayLengthValidator, ComplexArrayValidator, RangeValidator}
import ai.deepsense.deeplang.params.wrappers.spark.DoubleArrayParamWrapper

trait AFTSurvivalRegressionParams extends Params
    with PredictorParams
    with HasOptionalQuantilesColumnParam {

  val quantileProbabilities =
    new DoubleArrayParamWrapper[
        ml.param.Params { val quantileProbabilities: ml.param.DoubleArrayParam }](
      name = "quantile probabilities",
      description = Some("""Param for quantile probabilities array.
                      |Values of the quantile probabilities array should be in the range (0, 1)
                      |and the array should be non-empty.""".stripMargin),
      sparkParamGetter = _.quantileProbabilities,
      validator = ComplexArrayValidator(
        rangeValidator = RangeValidator(0, 1, beginIncluded = false, endIncluded = false),
        lengthValidator = ArrayLengthValidator.withAtLeast(1)
      ))
  setDefault(quantileProbabilities, Array(0.01, 0.05, 0.1, 0.25, 0.5, 0.75, 0.9, 0.95, 0.99))
}
