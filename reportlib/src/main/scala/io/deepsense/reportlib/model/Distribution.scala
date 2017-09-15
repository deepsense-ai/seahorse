/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.reportlib.model

sealed abstract class Distribution(
  val name: String,
  val subtype: String,
  val description: String)

sealed abstract class UnivariateDistribution(
  name: String,
  subtype: String,
  description: String,
  buckets: Seq[String],
  counts: Seq[Long]) extends Distribution(name, subtype, description)

case class CategoricalDistribution(
  override val name: String,
  override val description: String,
  buckets: Seq[String],
  counts: Seq[Long],
  override val subtype: String = CategoricalDistribution.subtype,
  blockType: String = DistributionJsonProtocol.typeName)
  extends UnivariateDistribution(
    name,
    CategoricalDistribution.subtype,
    description,
    buckets,
    counts) {
  require(subtype == CategoricalDistribution.subtype)
  require(blockType == DistributionJsonProtocol.typeName)
}

object CategoricalDistribution {
  val subtype = "categorical"
}

case class ContinuousDistribution(
  override val name: String,
  override val description: String,
  buckets: Seq[Double],
  counts: Seq[Long],
  statistics: Statistics,
  override val subtype: String = ContinuousDistribution.subtype,
  blockType: String = DistributionJsonProtocol.typeName)
  extends UnivariateDistribution(
    name,
    ContinuousDistribution.subtype,
    description,
    buckets.map(_.toString),
    counts) with ReportJsonProtocol {
  require(subtype == ContinuousDistribution.subtype)
  require(blockType == DistributionJsonProtocol.typeName)
}

object ContinuousDistribution {
  val subtype = "continuous"
}

case class Statistics(
  median: Double,
  max: Double,
  min: Double,
  mean: Double,
  firstQuartile: Double,
  thirdQuartile: Double,
  outliers: Seq[Double])
