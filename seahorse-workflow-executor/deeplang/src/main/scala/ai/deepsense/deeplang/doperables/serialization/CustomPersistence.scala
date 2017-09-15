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

import org.apache.spark.SparkContext

import ai.deepsense.commons.serialization.Serialization
import ai.deepsense.commons.utils.Logging

object CustomPersistence extends Logging {

  def save[T](sparkContext: SparkContext, instance: T, path: String): Unit = {
    val data: Array[Byte] = Serialization.serialize(instance)
    val rdd = sparkContext.parallelize(data, 1)
    rdd.saveAsTextFile(path)
  }

  def load[T](sparkContext: SparkContext, path: String): T = {
    logger.debug("Reading objects from: {}", path)
    val rdd = sparkContext.textFile(path)
    val data: Array[Byte] = rdd.map(_.toByte).collect()
    Serialization.deserialize(data)
  }
}
