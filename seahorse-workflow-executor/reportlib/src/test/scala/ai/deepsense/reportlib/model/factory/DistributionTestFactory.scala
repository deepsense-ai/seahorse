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

package ai.deepsense.reportlib.model.factory

import ai.deepsense.reportlib.model.{Distribution, DiscreteDistribution, ContinuousDistribution, Statistics}

trait DistributionTestFactory {

  def testCategoricalDistribution: Distribution =
    testCategoricalDistribution(DistributionTestFactory.distributionName)

  def testCategoricalDistribution(name: String): Distribution =
    DiscreteDistribution(
      name,
      DistributionTestFactory.distributionDescription,
      DistributionTestFactory.distributionMissingValues,
      DistributionTestFactory.categoricalDistributionBuckets,
      DistributionTestFactory.distributionCounts)

  def testContinuousDistribution: Distribution =
    testContinuousDistribution(DistributionTestFactory.distributionName)

  def testContinuousDistribution(name: String): Distribution =
    ContinuousDistribution(
      name,
      DistributionTestFactory.distributionDescription,
      DistributionTestFactory.distributionMissingValues,
      DistributionTestFactory.continuousDistributionBuckets,
      DistributionTestFactory.distributionCounts,
      testStatistics)

  val testStatistics: Statistics = Statistics("43", "1.5", "12.1")

  val testStatisticsWithEmptyValues: Statistics = Statistics(None, Some("1.5"), None)
}

object DistributionTestFactory extends DistributionTestFactory {
  val distributionName: String = "Distribution Name"
  val distributionDescription: String = "Some distribution description"
  val distributionMissingValues: Long = 0L
  val categoricalDistributionBuckets: List[String] =
    List("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
  val continuousDistributionBuckets: List[String] = List(
    "2", "2.5", "3", "3.5", "4", "4.5", "5", "5.5")
  val distributionCounts: List[Long] = List(0, 1000, 1, 17, 9786976976L, 0, 1)
}
