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

package ai.deepsense.commons.json

import java.util.UUID

import spray.httpx.SprayJsonSupport
import spray.json._

trait UUIDJsonProtocol {

  implicit object UUIDFormat extends JsonFormat[UUID] with SprayJsonSupport {
    override def write(obj: UUID): JsValue = JsString(obj.toString)

    override def read(json: JsValue): UUID = json match {
      case JsString(x) => UUID.fromString(x)
      case x => deserializationError(s"Expected UUID as JsString, but got $x")
    }
  }
}

object UUIDJsonProtocol extends UUIDJsonProtocol
