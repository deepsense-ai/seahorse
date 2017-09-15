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

import org.apache.spark.mllib.stat.MultivariateStatisticalSummary
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import ai.deepsense.commons.datetime.DateTimeConverter
import ai.deepsense.commons.utils.DoubleUtils
import ai.deepsense.deeplang.doperables.dataframe.report.distribution.{ColumnStats, DistributionBuilder}
import ai.deepsense.deeplang.utils.aggregators.Aggregator
import ai.deepsense.deeplang.utils.aggregators.AggregatorBatch.BatchedResult
import ai.deepsense.reportlib.model
import ai.deepsense.reportlib.model.{ContinuousDistribution, Distribution}

case class ContinuousDistributionBuilder(
   histogram: Aggregator[Array[Long], Row],
   missing: Aggregator[Long, Row],
   field: StructField,
   columnStats: ColumnStats)
  extends DistributionBuilder {

  def allAggregators: Seq[Aggregator[_, Row]] = Seq(histogram, missing)

  override def build(results: BatchedResult): Distribution = {
    val buckets = BucketsCalculator.calculateBuckets(field.dataType, columnStats)

    val histogramCounts = results.forAggregator(histogram)
    val nullsCount = results.forAggregator(missing)

    val labels = buckets2Labels(buckets, field)

    val stats = model.Statistics(
      double2Label(field)(columnStats.max),
      double2Label(field)(columnStats.min),
      mean2Label(field)(columnStats.mean))

    ContinuousDistribution(
      field.name,
      s"Continuous distribution for ${field.name} column",
      nullsCount,
      labels,
      histogramCounts,
      stats)
  }

  private def buckets2Labels(
    buckets: Seq[Double],
    structField: StructField): Seq[String] =
    buckets.map(double2Label(structField))

  /**
   * We want to present mean of integer-like values as a floating point number, however
   * dates, timestamps and booleans should be converted to their original type.
   */
  def mean2Label(structField: StructField)(d: Double): String = structField.dataType match {
    case ByteType | ShortType | IntegerType | LongType => DoubleUtils.double2String(d)
    case _ => double2Label(structField)(d)
  }

  def double2Label(structField: StructField)(d: Double): String = {
    if (d.isNaN) {
      "NaN"
    } else {
      structField.dataType match {
        case ByteType => d.toByte.toString
        case ShortType => d.toShort.toString
        case IntegerType => d.toInt.toString
        case LongType => d.toLong.toString
        case FloatType | DoubleType | _: DecimalType => DoubleUtils.double2String(d)
        case BooleanType => if (d == 0D) false.toString else true.toString
        case TimestampType | DateType =>
          DateTimeConverter.toString(DateTimeConverter.fromMillis(d.toLong))
      }
    }
  }

}
