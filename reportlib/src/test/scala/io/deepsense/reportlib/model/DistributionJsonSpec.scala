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

import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import spray.json._

import io.deepsense.reportlib.model.factory.DistributionTestFactory

class DistributionJsonSpec
  extends WordSpec
  with MockitoSugar
  with DistributionTestFactory
  with Matchers
  with ReportJsonProtocol {

  "CategoricalDistribution" should {
    val jsonCategoricalDistribution: JsObject = JsObject(
      "name" -> JsString(DistributionTestFactory.distributionName),
      "blockType" -> JsString("distribution"),
      "subtype" -> JsString("categorical"),
      "description" -> JsString(DistributionTestFactory.distributionDescription),
      "missingValues" -> JsNumber(0),
      "buckets" ->
        JsArray(DistributionTestFactory.categoricalDistributionBuckets.map(JsString(_)).toVector),
      "counts" -> JsArray(DistributionTestFactory.distributionCounts.map(JsNumber(_)).toVector)
    )
    "serialize to Json" in {
      val json = testCategoricalDistribution.toJson
      json shouldBe jsonCategoricalDistribution
    }
    "deserialize from Json" in {
      jsonCategoricalDistribution.convertTo[Distribution] shouldBe testCategoricalDistribution
    }
  }

  "ContinuousDistribution" should {
    val statistics = testStatistics
    val jsonContinuousDistribution: JsObject = JsObject(
      "name" -> JsString(DistributionTestFactory.distributionName),
      "blockType" -> JsString("distribution"),
      "subtype" -> JsString("continuous"),
      "description" -> JsString(DistributionTestFactory.distributionDescription),
      "missingValues" -> JsNumber(0),
      "buckets" ->
        JsArray(DistributionTestFactory.continuousDistributionBuckets.map(JsString(_)).toVector),
      "counts" -> JsArray(DistributionTestFactory.distributionCounts.map(JsNumber(_)).toVector),
      "statistics" -> expectedStatisticsJson(statistics)
    )
    "serialize to Json" in {
      val json = testContinuousDistribution.toJson
      json shouldBe jsonContinuousDistribution
    }
    "deserialize from Json" in {
      jsonContinuousDistribution.convertTo[Distribution] shouldBe testContinuousDistribution
    }
  }

  "Statistics" should {
    val statisticsWithEmptyValues = testStatisticsWithEmptyValues
    "serialize to Json" in {
      val json = statisticsWithEmptyValues.toJson
      json shouldBe expectedStatisticsJson(statisticsWithEmptyValues)
    }
    "deserialize from Json" in {
      expectedStatisticsJson(statisticsWithEmptyValues).convertTo[Statistics] shouldBe
        statisticsWithEmptyValues
    }
  }

  private def expectedStatisticsJson(statistics: Statistics): JsObject =
    JsObject(
      "median" -> jsStringOrNull(statistics.median),
      "max" -> jsStringOrNull(statistics.max),
      "min" -> jsStringOrNull(statistics.min),
      "mean" -> jsStringOrNull(statistics.mean),
      "firstQuartile" -> jsStringOrNull(statistics.firstQuartile),
      "thirdQuartile" -> jsStringOrNull(statistics.thirdQuartile),
      "outliers" -> JsArray(statistics.outliers.map(JsString(_)).toVector)
    )

  private def jsStringOrNull(s: Option[String]): JsValue = s.map(JsString(_)).getOrElse(JsNull)
}
