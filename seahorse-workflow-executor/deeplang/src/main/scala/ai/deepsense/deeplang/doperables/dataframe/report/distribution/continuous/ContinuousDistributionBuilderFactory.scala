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
import org.apache.spark.sql.types.StructField

import ai.deepsense.deeplang.doperables.dataframe.report.distribution._
import ai.deepsense.deeplang.utils.SparkTypeConverter._
import ai.deepsense.deeplang.utils.aggregators._

object ContinuousDistributionBuilderFactory {

  def prepareBuilder(
      columnIndex: Int,
      field: StructField,
      multivarStats: MultivariateStatisticalSummary): DistributionBuilder = {
    val columnStats = ColumnStats.fromMultiVarStats(multivarStats, columnIndex)
    // MultivarStats inits min with Double.MaxValue and max with MinValue.
    // If there is at least one not (null or NaN) its guaranteed to change min/max values.
    // TODO Its a bit hacky. Find more elegant solution. Example approaches:
    // - Filter out nulls? Problematic because we operate on vectors for performance.
    // - Remade spark aggregators to return options?
    val hasOnlyNulls =
      columnStats.min == Double.MaxValue &&
        columnStats.max == Double.MinValue

    if (!hasOnlyNulls) {
      val histogram = {
        val buckets = BucketsCalculator.calculateBuckets(field.dataType,
          columnStats)
        HistogramAggregator(buckets, true).mapInput(getColumnAsDouble(columnIndex))
      }
      val missing = CountOccurenceAggregator[Option[Any]](None).mapInput(getOption(columnIndex))
      val colStats = columnStats
      ContinuousDistributionBuilder(histogram, missing, field, colStats)
    } else {
      NoDistributionBuilder(field.name, NoDistributionReasons.OnlyNulls)
    }
  }
}
