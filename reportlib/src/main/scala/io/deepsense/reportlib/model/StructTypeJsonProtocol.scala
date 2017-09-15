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

import org.apache.spark.sql.types.{StructType, StructField, Metadata, DataType}
import spray.json._

trait StructTypeJsonProtocol extends DefaultJsonProtocol with NullOptions {

  implicit val dataTypeFormat = new RootJsonFormat[DataType] {
    override def write(obj: DataType): JsValue = obj.json.parseJson
    override def read(json: JsValue): DataType = DataType.fromJson(json.compactPrint)
  }

  implicit val metadataFormat = new RootJsonFormat[Metadata] {
    override def write(obj: Metadata): JsValue = obj.json.parseJson
    override def read(json: JsValue): Metadata = Metadata.fromJson(json.compactPrint)
  }

  implicit val structFieldFormat = jsonFormat4(StructField.apply)

  val structTypeConstructor: (Array[StructField] => StructType) = StructType.apply
  implicit val structTypeFormat = jsonFormat(structTypeConstructor, "fields")
}
