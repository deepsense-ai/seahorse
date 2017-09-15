/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.rest.json

import io.deepsense.deeplang.doperables.dataframe.{ColumnMetadata, DataFrameMetadata}
import io.deepsense.deeplang.doperables.dataframe.types.categorical.CategoriesMapping
import io.deepsense.deeplang.parameters.ColumnType._
import spray.httpx.SprayJsonSupport
import spray.json._

trait ColumnTypeJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit object ColumnTypeJsonFormat extends RootJsonFormat[ColumnType] {
    override def write(ct: ColumnType): JsValue = JsString(ct.toString)
    override def read(value: JsValue): ColumnType = throw new RuntimeException("Not supported")
  }
}

trait ColumnMetadataJsonProtocol
  extends DefaultJsonProtocol
  with SprayJsonSupport
  with ColumnTypeJsonProtocol
  with NullOptions {
  implicit val columnMetadataFormat = jsonFormat3(ColumnMetadata.apply)
}

trait CategoriesMappingJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit object CategoriesMappingFormat extends RootJsonFormat[CategoriesMapping] {
    override def write(cm: CategoriesMapping): JsValue =
      JsArray(cm.valueToId.keys.map(JsString(_)).toVector)
    override def read(value: JsValue): CategoriesMapping =
      throw new RuntimeException("Not supported")
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
  implicit object DataFrameMetadataFormat extends RootJsonFormat[DataFrameMetadata] {
    override def write(dfm: DataFrameMetadata): JsValue =
      JsObject(
        "isExact" -> dfm.isExact.toJson,
        "isColumnCountExact" -> dfm.isColumnCountExact.toJson,
        "columns" -> dfm.columns.toJson,
        "categoricalMappings" -> dfm.categoricalMappings.toJson)
    override def read(value: JsValue): DataFrameMetadata =
      throw new RuntimeException("Not supported")
  }
}

object DataFrameMetadataJsonProtocol extends DataFrameMetadataJsonProtocol
