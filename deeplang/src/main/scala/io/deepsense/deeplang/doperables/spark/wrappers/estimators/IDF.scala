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

import org.apache.spark.ml.feature.{IDF => SparkIDF, IDFModel => SparkIDFModel}

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.spark.wrappers.models.IDFModel
import io.deepsense.deeplang.doperables.spark.wrappers.params.common._
import io.deepsense.deeplang.doperables.{Report, SparkEstimatorWrapper}
import io.deepsense.deeplang.params.Param
import io.deepsense.deeplang.params.validators.RangeValidator
import io.deepsense.deeplang.params.wrappers.spark.IntParamWrapper

class IDF
  extends SparkEstimatorWrapper[SparkIDFModel, SparkIDF, IDFModel]
  with HasInputColumn
  with HasOutputColumn {

  val minDocFreq = new IntParamWrapper[SparkIDF](
    name = "min documents frequency",
    description = "The minimum of documents in which a term should appear",
    sparkParamGetter = _.minDocFreq,
    validator = RangeValidator(begin = 0.0, end = Int.MaxValue, step = Some(1.0)))
  setDefault(minDocFreq, 0.0)

  override def report(executionContext: ExecutionContext): Report = Report()

  override val params: Array[Param[_]] = declareParams(minDocFreq, inputColumn, outputColumn)
}
