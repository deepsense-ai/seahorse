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

package io.deepsense.reportlib.model

sealed abstract class Distribution(
  val name: String,
  val subtype: String,
  val description: String,
  val missingValues: Long)

sealed abstract class UnivariateDistribution(
    name: String,
    subtype: String,
    description: String,
    missingValues: Long,
    buckets: Seq[String],
    counts: Seq[Long])
  extends Distribution(name, subtype, description, missingValues)

case class DiscreteDistribution(
    override val name: String,
    override val description: String,
    override  val missingValues: Long,
    buckets: Seq[String],
    counts: Seq[Long],
    override val subtype: String = DiscreteDistribution.subtype,
    blockType: String = DistributionJsonProtocol.typeName)
  extends UnivariateDistribution(
    name,
    DiscreteDistribution.subtype,
    description,
    missingValues,
    buckets,
    counts) {
  require(subtype == DiscreteDistribution.subtype)
  require(blockType == DistributionJsonProtocol.typeName)
  require(buckets.size == counts.size, "buckets size does not match count size. " +
    s"Buckets size is: ${buckets.size}, counts size is: ${counts.size}")
}

object DiscreteDistribution {
  val subtype = "discrete"
}

case class ContinuousDistribution(
    override val name: String,
    override val description: String,
    override val missingValues: Long,
    buckets: Seq[String],
    counts: Seq[Long],
    statistics: Statistics,
    override val subtype: String = ContinuousDistribution.subtype,
    blockType: String = DistributionJsonProtocol.typeName)
  extends UnivariateDistribution(
    name,
    ContinuousDistribution.subtype,
    description,
    missingValues,
    buckets.map(_.toString),
    counts) with ReportJsonProtocol {
  require(subtype == ContinuousDistribution.subtype)
  require(blockType == DistributionJsonProtocol.typeName)
  require(buckets.size != 1, "Buckets size cannot be 1")
  require((buckets.isEmpty && counts.isEmpty)
    || (!buckets.isEmpty && (buckets.size == counts.size + 1)),
    "Either buckets and counts should be empty or " +
      "buckets size should be equal to count size + 1. " +
      s"Buckets size is: ${buckets.size}, counts size is: ${counts.size}")
}

object ContinuousDistribution {
  val subtype = "continuous"
}

case class Statistics(
  max: Option[String],
  min: Option[String],
  mean: Option[String])


object Statistics {
  def apply(): Statistics = new Statistics(None, None, None)

  def apply(max: String, min: String, mean: String): Statistics =
    Statistics(Option(max), Option(min), Option(mean))
}
