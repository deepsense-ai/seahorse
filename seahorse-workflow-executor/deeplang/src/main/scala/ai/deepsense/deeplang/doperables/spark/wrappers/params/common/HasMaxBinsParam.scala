/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperables.spark.wrappers.params.common

import scala.language.reflectiveCalls

import org.apache.spark.ml
import org.apache.spark.ml.regression.RandomForestRegressor

import ai.deepsense.deeplang.params.Params
import ai.deepsense.deeplang.params.validators.RangeValidator
import ai.deepsense.deeplang.params.wrappers.spark.{IntParamWrapper, LongParamWrapper}

trait HasMaxBinsParam extends Params {

  val maxBins = new IntParamWrapper[ml.param.Params { val maxBins: ml.param.IntParam }](
    name = "max bins",
    description = Some("The maximum number of bins used for discretizing continuous features " +
      "and for choosing how to split on features at each node. " +
      "More bins give higher granularity. " +
      "Must be >= 2 and >= number of categories in any categorical feature."),
    sparkParamGetter = _.maxBins,
    RangeValidator(2.0, Int.MaxValue, step = Some(1.0)))
  setDefault(maxBins, 32.0)

}
