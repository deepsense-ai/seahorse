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
import io.deepsense.deeplang.params.selections.NameSingleColumnSelection
import io.deepsense.deeplang.params.wrappers.spark.SingleColumnSelectorParamWrapper

trait HasRawPredictionColumnParam extends Params {

  val rawPredictionColumn =
    new SingleColumnSelectorParamWrapper[
        ml.param.Params { val rawPredictionCol: ml.param.Param[String] }](
      name = "raw prediction column",
      description = "Raw prediction (confidence) column",
      sparkParamGetter = _.rawPredictionCol,
      portIndex = 0)
  setDefault(rawPredictionColumn, NameSingleColumnSelection("rawPrediction"))
}
