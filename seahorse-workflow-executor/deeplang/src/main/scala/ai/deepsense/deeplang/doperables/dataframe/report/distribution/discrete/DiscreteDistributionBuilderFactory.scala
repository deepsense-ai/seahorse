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

package ai.deepsense.deeplang.doperables.dataframe.report.distribution.discrete

import org.apache.spark.sql.types.StructField

import ai.deepsense.deeplang.utils.SparkTypeConverter._
import ai.deepsense.deeplang.utils.aggregators._

private [distribution] object DiscreteDistributionBuilderFactory {

  val MaxDistinctValuesToCalculateDistribution = 10

  def prepareBuilder(
      columnIndex: Int,
      field: StructField): DiscreteDistributionBuilder = {
    val missing = CountOccurenceAggregator[Option[Any]](None)
      .mapInput(getOption(columnIndex))

    val categories = CountOccurrencesWithKeyLimitAggregator(
      MaxDistinctValuesToCalculateDistribution
    ).mapInput(getColumnAsString(columnIndex))

    DiscreteDistributionBuilder(categories, missing, field)
  }
}
