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

package io.deepsense.deeplang.utils

import scala.collection._
import scala.reflect.ClassTag

import org.apache.spark.rdd.RDD

import io.deepsense.deeplang.utils.aggregators.{CountOccurrencesWithKeyLimitAggregator, Aggregator}

/**
  * Utils for spark.
  */
object SparkUtils {

  /**
    * Counts occurrences of different values and outputs result as Map[T,Long].
    * Amount of keys in result map is limited by `limit`.
    * @return `None` if amount of distinct values is bigger than `limit`. <p>
    *         `Some(result)` if amount of distinct values is within `limit`.
    */
  def countOccurrencesWithKeyLimit[T](rdd: RDD[T], limit: Long): Option[Map[T, Long]] = {
    CountOccurrencesWithKeyLimitAggregator(limit).execute(rdd)
  }

}
