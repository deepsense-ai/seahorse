/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables.dataframe

import io.deepsense.deeplang.doperables.dataframe.types.categorical.CategoriesMapping
import io.deepsense.deeplang.parameters.ColumnType._
import spray.httpx.SprayJsonSupport
import spray.json._

trait ColumnTypeJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit object ColumnTypeJsonFormat extends RootJsonFormat[ColumnType] {
    override def write(ct: ColumnType): JsValue = JsString(ct.toString)
    override def read(value: JsValue): ColumnType = ???
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
    override def read(value: JsValue): CategoriesMapping = ???
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
