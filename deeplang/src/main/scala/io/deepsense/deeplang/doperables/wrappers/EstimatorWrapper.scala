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

package io.deepsense.deeplang.doperables.wrappers

import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.sql.types.StructType
import org.apache.spark.{ml, sql}

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.{Transformer, Estimator}
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.params.wrappers.deeplang.ParamWrapper

class EstimatorWrapper(
    executionContext: ExecutionContext,
    estimator: Estimator[Transformer])
  extends ml.Estimator[TransformerWrapper] {

  override def fit(dataset: sql.Dataset[_]): TransformerWrapper = {
    new TransformerWrapper(
      executionContext,
      estimator._fit(executionContext, DataFrame.fromSparkDataFrame(dataset.toDF())))
  }

  override def copy(extra: ParamMap): EstimatorWrapper = {
    val params = ParamTransformer.transform(extra)
    val estimatorCopy = estimator.replicate().set(params: _*)
    new EstimatorWrapper(executionContext, estimatorCopy)
  }

  override def transformSchema(schema: StructType): StructType = {
    schema
  }

  override lazy val params: Array[ml.param.Param[_]] = {
    estimator.params.map(new ParamWrapper(uid, _))
  }

  override val uid: String = Identifiable.randomUID("EstimatorWrapper")
}
