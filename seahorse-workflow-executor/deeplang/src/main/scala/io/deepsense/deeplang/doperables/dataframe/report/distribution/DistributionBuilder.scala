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

package io.deepsense.deeplang.doperables.dataframe.report.distribution

import org.apache.spark.sql.Row

import io.deepsense.deeplang.utils.aggregators.Aggregator
import io.deepsense.deeplang.utils.aggregators.AggregatorBatch.BatchedResult
import io.deepsense.reportlib.model.Distribution

trait DistributionBuilder {
  def allAggregators: Seq[Aggregator[_, Row]]
  def build(results: BatchedResult): Distribution
}
