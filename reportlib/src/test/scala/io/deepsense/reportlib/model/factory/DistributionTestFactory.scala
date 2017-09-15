/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.reportlib.model.factory

import io.deepsense.reportlib.model.{ContinuousDistribution, Statistics, CategoricalDistribution}

trait DistributionTestFactory {

  def testCategoricalDistribution: CategoricalDistribution =
    testCategoricalDistribution(DistributionTestFactory.distributionName)

  def testCategoricalDistribution(name: String): CategoricalDistribution = CategoricalDistribution(
    name,
    DistributionTestFactory.distributionDescription,
    DistributionTestFactory.categoricalDistributionBuckets,
    DistributionTestFactory.distributionCounts
  )

  def testContinuousDistribution: ContinuousDistribution =
    testContinuousDistribution(DistributionTestFactory.distributionName)

  def testContinuousDistribution(name: String): ContinuousDistribution = ContinuousDistribution(
    name,
    DistributionTestFactory.distributionDescription,
    DistributionTestFactory.continuousDistributionBuckets,
    DistributionTestFactory.distributionCounts,
    testStatistics
  )

  val testStatistics: Statistics =
    Statistics(15.4, 21, 1.5, 12.1, 17.1, 1.3, List(0.1, 0.2, 23.3, 23.5, 27.1))
}

object DistributionTestFactory extends DistributionTestFactory {
  val distributionName: String = "Distribution Name"
  val distributionDescription: String = "Some distribution description"
  val categoricalDistributionBuckets: List[String] =
    List("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
  val continuousDistributionBuckets: List[Double] = List(2, 2.5, 3, 3.5, 4, 4.5, 5)
  val distributionCounts: List[Long] = List(0, 1000, 1, 17, 9786976976L, 0, 1)
}
