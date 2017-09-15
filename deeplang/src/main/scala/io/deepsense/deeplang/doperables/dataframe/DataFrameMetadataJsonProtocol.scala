/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.dataframe

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.deeplang.doperables.dataframe.types.categorical.CategoriesMapping
import io.deepsense.deeplang.parameters.ColumnType
import io.deepsense.deeplang.parameters.ColumnType.ColumnType
import io.deepsense.commons.json.EnumerationSerializer

trait ColumnTypeJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {

}

trait CategoriesMappingJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit object CategoriesMappingFormat extends JsonFormat[CategoriesMapping] {
    override def write(cm: CategoriesMapping): JsValue = cm.valueToId.keys.toVector.toJson

    override def read(value: JsValue): CategoriesMapping =
      CategoriesMapping(value.convertTo[Seq[String]])
  }
}

trait ColumnMetadataJsonProtocol
  extends DefaultJsonProtocol
  with SprayJsonSupport
  with CategoriesMappingJsonProtocol
  with NullOptions {

  private val ColumnTypeField = "columnType"

  implicit val columnTypeFormat = EnumerationSerializer.jsonEnumFormat(ColumnType)

  implicit val commonColumnMetadataFormat = jsonFormat3(CommonColumnMetadata.apply)


  implicit object CategoricalColumnMetadataFormat extends JsonFormat[CategoricalColumnMetadata] {
    private val defaultFormat = jsonFormat3(CategoricalColumnMetadata.apply)
    override def write(cm: CategoricalColumnMetadata): JsValue = JsObject(
      cm.toJson(defaultFormat).asJsObject.fields + (ColumnTypeField -> cm.columnType.toJson))

    override def read(json: JsValue): CategoricalColumnMetadata =
      json.convertTo[CategoricalColumnMetadata](defaultFormat)
  }

  implicit object ColumnMetadataFormat extends JsonFormat[ColumnMetadata] {
    override def write(cm: ColumnMetadata): JsValue = cm match {
      case commonCm: CommonColumnMetadata => commonCm.toJson
      case categoricalCm: CategoricalColumnMetadata => categoricalCm.toJson
    }

    override def read(json: JsValue): ColumnMetadata = {
      val categoricalType = ColumnType.categorical.toString
      json.asJsObject.fields(ColumnTypeField) match {
        case JsNull => json.convertTo[CommonColumnMetadata]
        case notNullJson => notNullJson.convertTo[String] match {
          case `categoricalType` => json.convertTo[CategoricalColumnMetadata]
          case _ => json.convertTo[CommonColumnMetadata]
        }
      }
    }
  }
}

trait DataFrameMetadataJsonProtocol
    extends DefaultJsonProtocol
    with SprayJsonSupport
    with ColumnMetadataJsonProtocol {
  implicit val dataFrameMetadataFormat = jsonFormat3(DataFrameMetadata.apply)
}

object DataFrameMetadataJsonProtocol extends DataFrameMetadataJsonProtocol
