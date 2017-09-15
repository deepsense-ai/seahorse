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

import org.apache.spark.sql.types.Metadata
import spray.json._

trait MetadataJsonProtocol {
  implicit val metadataFormat = new RootJsonFormat[Metadata] {
    override def write(obj: Metadata): JsValue = obj.json.parseJson
    override def read(json: JsValue): Metadata = Metadata.fromJson(json.compactPrint)
  }
}

object MetadataJsonProtocol extends MetadataJsonProtocol
