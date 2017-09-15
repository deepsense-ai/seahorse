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

package ai.deepsense.deeplang.doperables.dataframe.report.distribution.continuous

import org.apache.spark.sql.types._

import ai.deepsense.deeplang.doperables.dataframe.report.distribution.ColumnStats

object BucketsCalculator {

  val DefaultBucketsNumber = 20
  val DoubleTolerance = 0.000001

  def calculateBuckets(dataType: DataType, columnStats: ColumnStats): Array[Double] = {
    val steps = numberOfSteps(columnStats, dataType)
    customRange(columnStats.min, columnStats.max, steps)
  }

  private def numberOfSteps(columnStats: ColumnStats, dataType: DataType): Int =
    if (columnStats.max - columnStats.min < DoubleTolerance) {
      1
    } else if (isIntegerLike(dataType)) {
      Math.min(
        columnStats.max.toLong - columnStats.min.toLong + 1,
        DefaultBucketsNumber).toInt
    } else {
      DefaultBucketsNumber
    }

  private def customRange(min: Double, max: Double, steps: Int): Array[Double] = {
    val span = max - min
    (Range.Int(0, steps, 1).map(s => min + (s * span) / steps) :+ max).toArray
  }

  private def isIntegerLike(dataType: DataType): Boolean =
    dataType match {
      case ByteType | ShortType | IntegerType | LongType | TimestampType | DateType => true
      case _ => false
    }
}
