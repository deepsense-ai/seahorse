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
import ai.deepsense.deeplang.params.wrappers.spark.{DoubleParamWrapper, IntParamWrapper}

trait HasMinInfoGainParam extends Params {

  val minInfoGain =
    new DoubleParamWrapper[ml.param.Params { val minInfoGain: ml.param.DoubleParam }](
      name = "min information gain",
      description = Some("The minimum information gain for a split to be considered at a tree node."),
      sparkParamGetter = _.minInfoGain,
      RangeValidator(0.0, Double.MaxValue))
  setDefault(minInfoGain, 0.0)

}
