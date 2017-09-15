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

package ai.deepsense.reportlib.model

import spray.json._

import ai.deepsense.commons.json.EnumerationSerializer
import ai.deepsense.commons.types.ColumnType

trait ReportJsonProtocol
  extends DefaultJsonProtocol
  with StructTypeJsonProtocol
  with ProductFormatsInstances
  with NullOptions {

  type maybeStats = Option[String]
  val statisticsConstructor: ((maybeStats, maybeStats, maybeStats) => Statistics) = Statistics.apply
  implicit val statisticsFormat = jsonFormat3(statisticsConstructor)
  implicit val reportTypeFormat = EnumerationSerializer.jsonEnumFormat(ReportType)

  implicit object DistributionJsonFormat extends JsonFormat[Distribution] {

    val categoricalDistributionFormat = jsonFormat(
      DiscreteDistribution.apply,
      DistributionJsonProtocol.nameKey,
      DistributionJsonProtocol.descriptionKey,
      DistributionJsonProtocol.missingValuesKey,
      DistributionJsonProtocol.bucketsKey,
      DistributionJsonProtocol.countsKey)

    val continuousDistributionFormat = jsonFormat(
      ContinuousDistribution.apply,
      DistributionJsonProtocol.nameKey,
      DistributionJsonProtocol.descriptionKey,
      DistributionJsonProtocol.missingValuesKey,
      DistributionJsonProtocol.bucketsKey,
      DistributionJsonProtocol.countsKey,
      DistributionJsonProtocol.statisticsKey)

    val noDistributionFormat = jsonFormat(
      NoDistribution.apply,
      DistributionJsonProtocol.nameKey,
      DistributionJsonProtocol.descriptionKey)

    override def read(json: JsValue): Distribution = {
      val fields: Map[String, JsValue] = json.asJsObject.fields
      val subtype: String = fields.get(DistributionJsonProtocol.subtypeKey).get.convertTo[String]
      subtype match {
        case DiscreteDistribution.subtype => json.convertTo(categoricalDistributionFormat)
        case ContinuousDistribution.subtype => json.convertTo(continuousDistributionFormat)
        case NoDistribution.subtype => json.convertTo(noDistributionFormat)
      }
    }
    override def write(distribution: Distribution): JsValue = {
      val basicFields = Map(
        DistributionJsonProtocol.nameKey -> JsString(distribution.name),
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
        case d: DiscreteDistribution => JsObject(basicFields ++ Map(
          DistributionJsonProtocol.countsKey -> d.counts.toJson,
          DistributionJsonProtocol.bucketsKey -> d.categories.toJson
        ))
        case d: NoDistribution => JsObject(basicFields)
      }
    }
  }

  implicit val columnTypeFormat = new RootJsonFormat[ColumnType.ColumnType] {
    override def write(obj: ColumnType.ColumnType): JsValue = {
      JsString(obj.toString)
    }

    override def read(json: JsValue): ColumnType.ColumnType = json match {
      case JsString(str) => ColumnType.withName(str)
      case _ => throw new DeserializationException("Enum string expected")
    }
  }

  implicit val tableFormat = jsonFormat6(Table.apply)
  implicit val reportFormat = jsonFormat4(ReportContent.apply)
}

object ReportJsonProtocol extends ReportJsonProtocol

object DistributionJsonProtocol {
  val typeName = "distribution"
  val nameKey = "name"
  val reportTypeKey = "reportType"
  val subtypeKey = "subtype"
  val missingValuesKey = "missingValues"
  val descriptionKey = "description"
  val bucketsKey = "buckets"
  val countsKey = "counts"
  val statisticsKey = "statistics"
}
