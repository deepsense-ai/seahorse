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

package io.deepsense.deeplang.doperables.spark.wrappers.params.common

import scala.language.reflectiveCalls

import org.apache.spark.ml

import io.deepsense.deeplang.params.Params
import io.deepsense.deeplang.params.wrappers.spark.BooleanParamWrapper

trait StandardScalerCommonParams extends Params {

  val withMean = new BooleanParamWrapper[ml.param.Params { val withMean: ml.param.BooleanParam }](
    name = "with mean",
    description = "Centers the data with mean before scaling",
    sparkParamGetter = _.withMean)
  setDefault(withMean, false)

  val withStd = new BooleanParamWrapper[ml.param.Params { val withStd: ml.param.BooleanParam }](
    name = "with std",
    description = "Scales the data to unit standard deviation",
    sparkParamGetter = _.withStd)
  setDefault(withStd, true)
}
