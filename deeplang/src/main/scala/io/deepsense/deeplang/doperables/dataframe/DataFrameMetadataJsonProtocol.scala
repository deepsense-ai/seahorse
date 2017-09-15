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

package io.deepsense.deeplang.doperables.dataframe

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.commons.json.EnumerationSerializer
import io.deepsense.deeplang.doperables.dataframe.types.categorical.CategoriesMapping
import io.deepsense.deeplang.parameters.ColumnType

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
