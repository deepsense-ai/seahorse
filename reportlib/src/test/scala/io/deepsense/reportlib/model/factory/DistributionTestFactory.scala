/**
 * Copyright 2015, CodiLime Inc.
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

package io.deepsense.reportlib.model.factory

import io.deepsense.reportlib.model.{ContinuousDistribution, Statistics, CategoricalDistribution}

trait DistributionTestFactory {

  def testCategoricalDistribution: CategoricalDistribution =
    testCategoricalDistribution(DistributionTestFactory.distributionName)

  def testCategoricalDistribution(name: String): CategoricalDistribution =
    CategoricalDistribution(
      name,
      DistributionTestFactory.distributionDescription,
      DistributionTestFactory.distributionMissingValues,
      DistributionTestFactory.categoricalDistributionBuckets,
      DistributionTestFactory.distributionCounts)

  def testContinuousDistribution: ContinuousDistribution =
    testContinuousDistribution(DistributionTestFactory.distributionName)

  def testContinuousDistribution(name: String): ContinuousDistribution =
    ContinuousDistribution(
      name,
      DistributionTestFactory.distributionDescription,
      DistributionTestFactory.distributionMissingValues,
      DistributionTestFactory.continuousDistributionBuckets,
      DistributionTestFactory.distributionCounts,
      testStatistics)

  val testStatistics: Statistics =
    Statistics("5.4", "43", "1.5", "12.1", "1", "1.23", List("0.1", "0.2", "23.3", "23.5", "27.1"))

  val testStatisticsWithEmptyValues: Statistics =
    Statistics(Some("5.4"), Some("43"), Some("1.5"), Some("12.1"), None, None, List())
}

object DistributionTestFactory extends DistributionTestFactory {
  val distributionName: String = "Distribution Name"
  val distributionDescription: String = "Some distribution description"
  val distributionMissingValues: Long = 0L
  val categoricalDistributionBuckets: List[String] =
    List("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
  val continuousDistributionBuckets: List[String] = List("2", "2.5", "3", "3.5", "4", "4.5", "5")
  val distributionCounts: List[Long] = List(0, 1000, 1, 17, 9786976976L, 0, 1)
}
