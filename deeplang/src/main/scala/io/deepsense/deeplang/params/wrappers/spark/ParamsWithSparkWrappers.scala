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

package io.deepsense.deeplang.params.wrappers.spark

import org.apache.spark.ml
import org.apache.spark.sql.types.StructType

import io.deepsense.deeplang.params.Params

trait ParamsWithSparkWrappers[P <: ml.param.Params] extends Params {
  lazy val sparkParamWrappers: Array[SparkParamWrapper[_, _, _]] = params.collect {
    case wrapper: SparkParamWrapper[_, _, _] => wrapper +: wrapper.nestedWrappers
  }.flatten

  def sparkParamPairs(sparkEntity: P, schema: StructType): Array[ml.param.ParamPair[Any]] =
    sparkParamWrappers.map(wrapper => {
      val value = $(wrapper)
      val convertedValue = wrapper.convertAny(value)(schema)
      ml.param.ParamPair(
        wrapper.sparkParam(sparkEntity).asInstanceOf[ml.param.Param[Any]], convertedValue)
    })
}
