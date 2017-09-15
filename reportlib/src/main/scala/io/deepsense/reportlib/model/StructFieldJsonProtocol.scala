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

import org.apache.spark.sql.types.{Metadata, StructField}
import spray.json._

trait StructFieldJsonProtocol
  extends DefaultJsonProtocol
  with MetadataJsonProtocol
  with DataTypeJsonProtocol {

  // StructField format without metadata
  implicit val structFieldFormat = new RootJsonFormat[StructField] {
    val rawFormat = jsonFormat4(StructField.apply)

    override def write(obj: StructField): JsValue = {
      JsObject(obj.toJson(rawFormat).asJsObject.fields.collect {
        case (key, value) if key != "metadata" => (key, value)
      })
    }

    override def read(json: JsValue): StructField = {
      val fields = json.asJsObject.fields + ("metadata" -> Metadata.empty.toJson)
      JsObject(fields).convertTo(rawFormat)
    }
  }
}
