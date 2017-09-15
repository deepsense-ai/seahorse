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

import org.apache.spark.sql.types.{DataType, StructField}
import spray.json._

import ai.deepsense.commons.json.EnumerationSerializer
import ai.deepsense.commons.types.{ColumnType, SparkConversions}

trait StructFieldJsonProtocol
  extends DefaultJsonProtocol
  with MetadataJsonProtocol
  with DataTypeJsonProtocol {

  implicit val failureCodeFormat = EnumerationSerializer.jsonEnumFormat(ColumnType)

  // StructField format without metadata, with deeplangType appended
  implicit val structFieldFormat = new RootJsonFormat[StructField] {
    val c = (s: String, d: DataType, b: Boolean) => StructField(s, d, b)
    implicit val rawFormat = jsonFormat(c, "name", "dataType", "nullable")

    override def write(obj: StructField): JsValue = {
      val jsObject = obj.toJson(rawFormat).asJsObject

      val deeplangType =
        SparkConversions.sparkColumnTypeToColumnType(obj.dataType)

      JsObject(jsObject.fields + ("deeplangType" -> deeplangType.toJson))
    }

    override def read(json: JsValue): StructField = {
      json.convertTo(rawFormat)
    }
  }
}
