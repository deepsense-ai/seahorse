/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.reportlib.model

import spray.json._

trait ReportJsonProtocol extends DefaultJsonProtocol with ProductFormatsInstances with NullOptions {

  implicit val statisticsFormat = jsonFormat7(Statistics)
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
      distribution match  {
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
  implicit val reportFormat = jsonFormat3(ReportContent)
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
