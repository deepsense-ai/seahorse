/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.dataframe

import io.deepsense.commons.json.EnumerationSerializer
import io.deepsense.deeplang.doperables.dataframe.types.categorical.CategoriesMapping
import io.deepsense.deeplang.parameters.ColumnType
import spray.httpx.SprayJsonSupport
import spray.json._
import spray.json.DefaultJsonProtocol._


trait ColumnMetadataJsonProtocol
  extends DefaultJsonProtocol
  with SprayJsonSupport
  with NullOptions {
  implicit val columnTypeFormat = EnumerationSerializer.jsonEnumFormat(ColumnType)
  implicit val columnMetadataFormat = jsonFormat3(ColumnMetadata.apply)
}

trait CategoriesMappingJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit object CategoriesMappingFormat extends RootJsonFormat[CategoriesMapping] {
    override def write(cm: CategoriesMapping): JsValue =
      JsArray(cm.valueToId.keys.map(JsString(_)).toVector)
    override def read(value: JsValue): CategoriesMapping = {
      value match {
        case jsArray: JsArray => CategoriesMapping(jsArray.convertTo[List[String]])
        case _ => throw new DeserializationException("JsArray expected")
      }
    }
  }
}

/**
 * Serialization protocol for metadata inference.
 * Does not support read.
 */
trait DataFrameMetadataJsonProtocol
    extends DefaultJsonProtocol
    with SprayJsonSupport
    with ColumnMetadataJsonProtocol
    with CategoriesMappingJsonProtocol {
  implicit val dataFrameMetadataFormat = jsonFormat4(DataFrameMetadata.apply)
}

object DataFrameMetadataJsonProtocol extends DataFrameMetadataJsonProtocol
