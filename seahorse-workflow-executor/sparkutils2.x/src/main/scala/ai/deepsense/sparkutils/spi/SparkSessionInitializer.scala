/**
 * Copyright 2018 Astraea, Inc.
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

package ai.deepsense.sparkutils.spi

import java.util.ServiceLoader

import org.apache.spark.sql.SparkSession
import scala.collection.JavaConversions._

/**
  * SPI Interface for services wishing to tweak the SparkSession after its created
  * (e.g. for registering UDFs).
  *
  * @since 5/22/18
  */
trait SparkSessionInitializer {
  def init(sparkSession: SparkSession): Unit
}

object SparkSessionInitializer {
  def apply(sparkSession: SparkSession): SparkSession = {

    val initializers = ServiceLoader.load(classOf[SparkSessionInitializer])
    for(initter <- initializers) {
      initter.init(sparkSession)
    }
    sparkSession
  }
}
