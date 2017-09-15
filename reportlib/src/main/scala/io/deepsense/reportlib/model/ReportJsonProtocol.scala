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

import spray.json._

trait ReportJsonProtocol extends DefaultJsonProtocol with ProductFormatsInstances with NullOptions {
  implicit val statisticsFormat = jsonFormat7(
    Statistics.apply(
      _: Option[String],
      _: Option[String],
      _: Option[String],
      _: Option[String],
      _: Option[String],
      _: Option[String],
      _: Seq[String]))
  implicit val categoricalDistributionFormat = jsonFormat(
    CategoricalDistribution.apply,
    DistributionJsonProtocol.nameKey,
    DistributionJsonProtocol.descriptionKey,
    DistributionJsonProtocol.missingValuesKey,
    DistributionJsonProtocol.bucketsKey,
    DistributionJsonProtocol.countsKey,
    DistributionJsonProtocol.subtypeKey,
    DistributionJsonProtocol.typeKey)
  implicit val continuousDistributionFormat = jsonFormat(
    ContinuousDistribution.apply,
    DistributionJsonProtocol.nameKey,
    DistributionJsonProtocol.descriptionKey,
    DistributionJsonProtocol.missingValuesKey,
    DistributionJsonProtocol.bucketsKey,
    DistributionJsonProtocol.countsKey,
    DistributionJsonProtocol.statisticsKey,
    DistributionJsonProtocol.subtypeKey,
    DistributionJsonProtocol.typeKey)

  implicit object DistributionJsonReader extends JsonFormat[Distribution] {
    override def read(json: JsValue): Distribution = {
      val fields: Map[String, JsValue] = json.asJsObject.fields
      require(fields.get(DistributionJsonProtocol.typeKey) ==
      Some(JsString(DistributionJsonProtocol.typeName)))
      val subtype: String = fields.get(DistributionJsonProtocol.subtypeKey).get.convertTo[String]
      subtype match {
        case CategoricalDistribution.subtype => json.convertTo[CategoricalDistribution]
        case ContinuousDistribution.subtype => json.convertTo[ContinuousDistribution]
      }
    }
    override def write(distribution: Distribution): JsValue = {
      val basicFields = Map(
        DistributionJsonProtocol.nameKey -> JsString(distribution.name),
        DistributionJsonProtocol.typeKey -> JsString(DistributionJsonProtocol.typeName),
        DistributionJsonProtocol.subtypeKey -> JsString(distribution.subtype),
        DistributionJsonProtocol.descriptionKey -> JsString(distribution.description),
        DistributionJsonProtocol.missingValuesKey -> JsNumber(distribution.missingValues)
      )
      distribution match {
        case d: ContinuousDistribution => JsObject(basicFields ++ Map(
          DistributionJsonProtocol.bucketsKey -> d.buckets.toJson,
          DistributionJsonProtocol.countsKey -> d.counts.toJson,
          DistributionJsonProtocol.statisticsKey -> d.statistics.toJson
        ))
        case d: CategoricalDistribution => JsObject(basicFields ++ Map(
          DistributionJsonProtocol.countsKey -> d.counts.toJson,
          DistributionJsonProtocol.bucketsKey -> d.buckets.toJson
        ))
      }
    }
  }
  implicit val tableFormat = jsonFormat6(Table.apply)
  implicit val reportFormat = jsonFormat3(ReportContent.apply)
}

object ReportJsonProtocol extends ReportJsonProtocol

object DistributionJsonProtocol {
  val typeName = "distribution"
  val nameKey = "name"
  val typeKey = "blockType"
  val subtypeKey = "subtype"
  val missingValuesKey = "missingValues"
  val descriptionKey = "description"
  val bucketsKey = "buckets"
  val countsKey = "counts"
  val statisticsKey = "statistics"
}
