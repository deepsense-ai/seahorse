/**
 * Copyright 2016, deepsense.io
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

package io.deepsense.deeplang.doperables.spark.wrappers.estimators

import scala.reflect.runtime.universe.TypeTag

import org.apache.spark.ml
import org.apache.spark.ml.Model

import io.deepsense.deeplang.doperables.{Transformer, Estimator}
import io.deepsense.deeplang.params.wrappers.spark.ParamsWithSparkWrappers

abstract class SimpleSparkEstimatorWrapper[M <: Model[M], T <: Transformer]
    ()(implicit typeTag: TypeTag[T])
  extends Estimator[T]
  with ParamsWithSparkWrappers {

  def sparkEstimator: ml.Estimator[M]
}
