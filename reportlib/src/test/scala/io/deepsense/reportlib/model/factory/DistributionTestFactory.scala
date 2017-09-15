/**
 * Copyright (c) 2015, CodiLime Inc.
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
