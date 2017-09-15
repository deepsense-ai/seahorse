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

import ai.deepsense.deeplang.params.Params
import ai.deepsense.deeplang.params.wrappers.spark.DoubleParamWrapper

trait MinMaxParams extends Params {

  val min = new DoubleParamWrapper[ml.param.Params { val min: ml.param.DoubleParam }](
    name = "min",
    description = Some("The lower bound after transformation, shared by all features."),
    sparkParamGetter = _.min)
  setDefault(min, 0.0)

  val max = new DoubleParamWrapper[ml.param.Params { val max: ml.param.DoubleParam }](
    name = "max",
    description = Some("The upper bound after transformation, shared by all features."),
    sparkParamGetter = _.max)
  setDefault(max, 1.0)

  def setMin(value: Double): this.type = {
    set(min, value)
  }

  def setMax(value: Double): this.type = {
    set(max, value)
  }
}
