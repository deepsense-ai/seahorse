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

package io.deepsense.deeplang.doperables.spark.wrappers.models

import org.apache.spark.ml
import org.apache.spark.ml.feature.{PCA => SparkPCA, PCAModel => SparkPCAModel}

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.spark.wrappers.params.common.{HasOutputCol, HasInputCol}
import io.deepsense.deeplang.params.validators.RangeValidator
import io.deepsense.deeplang.params.wrappers.spark.IntParamWrapper

import io.deepsense.deeplang.doperables.{Report, SparkModelWrapper}
import io.deepsense.deeplang.params.Param

class PCAModel
  extends SparkModelWrapper[SparkPCAModel, SparkPCA]
  with HasInputCol
  with HasOutputCol {

  val k = new IntParamWrapper[ml.param.Params { val k: ml.param.IntParam }](
    name = "k",
    description = "Number of principal components",
    sparkParamGetter = _.k,
    validator = RangeValidator.positiveIntegers)

  override def report(executionContext: ExecutionContext): Report = Report()

  override val params: Array[Param[_]] = declareParams(k, inputCol, outputCol)
}
