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

package io.deepsense.deeplang.doperables

import scala.reflect.runtime.universe.TypeTag

import org.apache.spark.ml

import io.deepsense.deeplang.doperables.multicolumn.HasSpecificParams
import io.deepsense.deeplang.doperables.spark.wrappers.params.common.{HasInputColumn, HasOutputColumn}
import io.deepsense.deeplang.params.Param
import io.deepsense.deeplang.params.wrappers.spark.ParamsWithSparkWrappers

abstract class SparkSingleColumnEstimatorWrapper[
    MD <: ml.Model[MD],
    E <: ml.Estimator[MD],
    MW <: SparkSingleColumnModelWrapper[MD, E]]
(override implicit val modelWrapperTag: TypeTag[MW], override implicit val estimatorTag: TypeTag[E])
  extends SparkEstimatorWrapper[MD, E, MW]
  with ParamsWithSparkWrappers
  with HasInputColumn
  with HasOutputColumn
  with HasSpecificParams {

  override lazy val params: Array[Param[_]] =
    declareParams(Array(inputColumn, outputColumn) ++ getSpecificParams: _*)
}

