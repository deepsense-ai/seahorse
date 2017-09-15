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

package io.deepsense.deeplang.doperables.dataframe.report.distribution.discrete

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StructField, BooleanType, StringType, StructType}

import io.deepsense.deeplang.doperables.dataframe.report.{DataFrameReportGenerator, ReportUtils}
import io.deepsense.deeplang.doperables.dataframe.report.distribution.{NoDistributionReasons, DistributionBuilder}
import io.deepsense.deeplang.utils.aggregators.Aggregator
import io.deepsense.deeplang.utils.aggregators.AggregatorBatch.BatchedResult
import io.deepsense.reportlib.model.{NoDistribution, DiscreteDistribution, Distribution}

case class DiscreteDistributionBuilder(
    categories: Aggregator[Option[scala.collection.mutable.Map[String, Long]], Row],
    missing: Aggregator[Long, Row],
    field: StructField)
  extends DistributionBuilder {

  def allAggregators: Seq[Aggregator[_, Row]] = Seq(categories, missing)

  override def build(results: BatchedResult): Distribution = {
    val categoriesMap = results.forAggregator(categories)
    val nullsCount = results.forAggregator(missing)

    categoriesMap match {
      case Some(occurrencesMap) => {
        val labels = field.dataType match {
          case StringType => occurrencesMap.keys.toSeq.sorted
          // We always want two labels, even when all elements are true or false
          case BooleanType => Seq(false.toString, true.toString)
        }
        val counts = labels.map(occurrencesMap.getOrElse(_, 0L))
        DiscreteDistribution(
          field.name,
          s"Discrete distribution for ${field.name} column",
          nullsCount,
          labels.map(ReportUtils.shortenLongStrings(_,
            DataFrameReportGenerator.StringPreviewMaxLength)),
          counts)
      }
      case None => NoDistribution(
        field.name,
        NoDistributionReasons.TooManyDistinctCategoricalValues
      )
    }
  }
}


