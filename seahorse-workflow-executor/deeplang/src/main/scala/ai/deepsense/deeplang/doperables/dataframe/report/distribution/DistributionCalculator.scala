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

package ai.deepsense.deeplang.doperables.dataframe.report.distribution

import org.apache.spark.mllib.stat.MultivariateStatisticalSummary
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types._

import ai.deepsense.deeplang.doperables.dataframe.report.distribution.continuous.ContinuousDistributionBuilderFactory
import ai.deepsense.deeplang.doperables.dataframe.report.distribution.discrete.DiscreteDistributionBuilderFactory
import ai.deepsense.deeplang.utils.aggregators.AggregatorBatch
import ai.deepsense.reportlib.model._

object DistributionCalculator {

  def distributionByColumn(
    sparkDataFrame: org.apache.spark.sql.DataFrame,
    multivarStats: MultivariateStatisticalSummary): Map[String, Distribution] = {
    val dataFrameEmpty = multivarStats.count == 0

    if (dataFrameEmpty) {
      noDistributionBecauseOfNoData(sparkDataFrame.schema)
    } else {
      distributionForNonEmptyDataFrame(sparkDataFrame, multivarStats)
    }
  }

  private def noDistributionBecauseOfNoData(schema: StructType): Map[String, Distribution] = {
    for (columnName <- schema.fieldNames) yield {
      columnName -> NoDistribution(
        columnName,
        NoDistributionReasons.NoData
      )
    }
  }.toMap

  /**
   * Some calculations needed to obtain distributions can be performed together which
   * would result in only one pass over data.
   * <p>
   * To achieve that 'Aggregator' abstraction was introduced.
   * It contains all methods needed for rdd::aggregate method.
   * By abstracting it it's possible to batch together aggregators in a generic way.
   * <p>
   * Factory classes returns BUILDERS that have all data needed for manufacturing Distributions
   * except for data needed to be calculated on clusters. Builders expose their
   * internal aggregators for those instead.
   * <p>
   * All aggregators from builders are collected here, batched together and calculated in one pass.
   * Then batched result is passed to DistributionBuilders and final Distributions objects
   * are made.
   */
  private def distributionForNonEmptyDataFrame(
    sparkDataFrame: DataFrame,
    multivarStats: MultivariateStatisticalSummary): Map[String, Distribution] = {
    val schema = sparkDataFrame.schema

    val distributionBuilders = for {
      (structField, columnIndex) <- sparkDataFrame.schema.zipWithIndex
    } yield {
      DistributionType.forStructField(structField) match {
        case DistributionType.Discrete =>
          DiscreteDistributionBuilderFactory.prepareBuilder(columnIndex, structField)
        case DistributionType.Continuous =>
          ContinuousDistributionBuilderFactory.prepareBuilder(
            columnIndex, structField, multivarStats)
        case DistributionType.NotApplicable => NoDistributionBuilder(
          structField.name,
          NoDistributionReasons.NotApplicableForType(structField.dataType))
      }
    }
    val results = {
      val aggregators = distributionBuilders.flatMap(_.allAggregators)
      AggregatorBatch.executeInBatch(sparkDataFrame.rdd, aggregators)
    }
    val distributions = distributionBuilders.map(_.build(results))
    distributions.map(d => d.name -> d).toMap
  }
}
