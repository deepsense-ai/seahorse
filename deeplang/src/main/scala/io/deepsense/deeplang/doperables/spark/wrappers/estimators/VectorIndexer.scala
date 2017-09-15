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

package io.deepsense.deeplang.doperables.spark.wrappers.estimators

import org.apache.spark.ml.feature.{VectorIndexer => SparkVectorIndexer, VectorIndexerModel => SparkVectorIndexerModel}

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.spark.wrappers.models.VectorIndexerModel
import io.deepsense.deeplang.doperables.spark.wrappers.params.common._
import io.deepsense.deeplang.doperables.{Report, SparkEstimatorWrapper}
import io.deepsense.deeplang.params.Param
import io.deepsense.deeplang.params.validators.RangeValidator
import io.deepsense.deeplang.params.wrappers.spark.IntParamWrapper

class VectorIndexer
  extends SparkEstimatorWrapper[SparkVectorIndexerModel, SparkVectorIndexer, VectorIndexerModel]
  with HasInputColumn
  with HasOutputColumn {

  val maxCategories = new IntParamWrapper[SparkVectorIndexer](
    name = "max categories",
    description = "Threshold for the number of values a categorical feature can take. " +
      "If a feature is found to have > maxCategories values, then it is declared continuous. " +
      "Must be >= 2",
    sparkParamGetter = _.maxCategories,
    validator = RangeValidator(begin = 2.0, end = Int.MaxValue, step = Some(1.0)))
  setDefault(maxCategories, 20.0)

  override def report(executionContext: ExecutionContext): Report = Report()

  override val params: Array[Param[_]] = declareParams(maxCategories, inputColumn, outputColumn)
}
