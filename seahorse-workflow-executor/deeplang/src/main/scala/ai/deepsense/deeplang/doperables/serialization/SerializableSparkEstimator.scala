/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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


package ai.deepsense.deeplang.doperables.serialization

import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.util.{MLWritable, MLWriter}
import org.apache.spark.ml.{Estimator, Model}
import org.apache.spark.sql
import org.apache.spark.sql.types.StructType

import ai.deepsense.sparkutils.ML

class SerializableSparkEstimator[T <: Model[T], E <: Estimator[T]](val sparkEstimator: E)
  extends ML.Estimator[SerializableSparkModel[T]]
  with MLWritable {

  override val uid: String = "e2a121fe-da6e-4ef2-9c5e-56ee558c14f0"

  override def fitDF(dataset: sql.DataFrame): SerializableSparkModel[T] = {
    val result: T = sparkEstimator.fit(dataset)
    new SerializableSparkModel[T](result)
  }

  override def copy(extra: ParamMap): Estimator[SerializableSparkModel[T]] =
    new SerializableSparkEstimator[T, E](sparkEstimator.copy(extra).asInstanceOf[E])

  override def write: MLWriter = new DefaultMLWriter(this)

  override def transformSchema(schema: StructType): StructType =
    sparkEstimator.transformSchema(schema)
}
