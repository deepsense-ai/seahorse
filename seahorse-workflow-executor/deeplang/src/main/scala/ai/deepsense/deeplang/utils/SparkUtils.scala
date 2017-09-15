/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.utils

import scala.collection._

import org.apache.spark.rdd.RDD

import ai.deepsense.deeplang.utils.aggregators.CountOccurrencesWithKeyLimitAggregator

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

  /**
    * Returns Spark's DataFrame column name safe for using in SQL expressions.
    * @param columnName
    * @return properly escaped column name
    */
  def escapeColumnName(columnName: String): String = {
    // We had to forbid backticks in column names due to anomalies in Spark 1.6
    // See: https://issues.apache.org/jira/browse/SPARK-13297
    // "`" + columnName.replace("`", "``") + "`"
    "`" + columnName + "`"
  }
}
