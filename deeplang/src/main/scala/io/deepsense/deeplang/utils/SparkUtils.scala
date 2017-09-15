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

import org.apache.spark.rdd.RDD

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

    // Accumulate allows seq and comb function to mutate first argument and return it.
    // This approach saves memory allocations while aggregating data.

    def seq[T](
        accOpt: Option[mutable.Map[T, Long]],
        next: T): Option[mutable.Map[T, Long]] = {
      accOpt.foreach { acc =>
        addOccurrencesToMap(acc, next, 1)
      }
      replacedWithNoneIfLimitExceeded(accOpt)
    }

    def comb[T](
        leftOpt: Option[mutable.Map[T, Long]],
        rightOpt: Option[mutable.Map[T, Long]]): Option[mutable.Map[T, Long]] = {
      for (left <- leftOpt; rightMap <- rightOpt) {
        rightMap.foreach { case (element, count) =>
          addOccurrencesToMap(left, element, count)
        }
      }
      replacedWithNoneIfLimitExceeded(leftOpt)
    }

    def addOccurrencesToMap[T](
        occurrences: mutable.Map[T, Long],
        element: T,
        count: Long): Unit = {
      occurrences(element) = occurrences.getOrElse(element, 0L) + count
    }


    def replacedWithNoneIfLimitExceeded[T](
        mapOpt: Option[mutable.Map[T, Long]]): Option[mutable.Map[T, Long]] = {
      mapOpt.flatMap { map =>
        if (map.size <= limit) {
          mapOpt
        } else {
          None
        }
      }
    }

    rdd.aggregate(Option(mutable.Map.empty[T, Long]))(seq _, comb _)
  }

}
