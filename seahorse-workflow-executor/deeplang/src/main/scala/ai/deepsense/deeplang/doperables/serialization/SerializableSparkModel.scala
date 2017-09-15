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

import org.apache.spark.ml.Model
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.util.{MLReadable, MLReader, MLWritable, MLWriter}
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types.StructType

import ai.deepsense.sparkutils.ML

class SerializableSparkModel[M <: Model[M]](val sparkModel: M)
  extends ML.Model[SerializableSparkModel[M]]
  with MLWritable {

  override def copy(extra: ParamMap): SerializableSparkModel[M] =
    new SerializableSparkModel(sparkModel.copy(extra))

  override def write: MLWriter = {
    sparkModel match {
      case w: MLWritable => w.write
      case _ => new DefaultMLWriter(this)
    }
  }

  override def transformDF(dataset: DataFrame): DataFrame = sparkModel.transform(dataset)

  override def transformSchema(schema: StructType): StructType = sparkModel.transformSchema(schema)

  override val uid: String = "dc7178fe-b209-44f5-8a74-d3c4dafa0fae"
}

// This class may seem unused, but it is used reflectively by spark deserialization mechanism
object SerializableSparkModel extends MLReadable[SerializableSparkModel[_]] {
  override def read: MLReader[SerializableSparkModel[_]] = {
    new DefaultMLReader[SerializableSparkModel[_]]()
  }
}
