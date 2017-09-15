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

import org.joda.time.DateTime
import spray.httpx.SprayJsonSupport
import spray.json._

import ai.deepsense.commons.datetime.DateTimeConverter


trait DateTimeJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {

  implicit object DateTimeJsonFormat extends JsonFormat[DateTime] {

    override def write(obj: DateTime): JsValue = {
      JsString(DateTimeConverter.toString(obj))
    }

    override def read(json: JsValue): DateTime = json match {
      case JsString(value) =>
        DateTimeConverter.parseDateTime(value)
      case x => throw new DeserializationException(
        s"Expected JsString with DateTime in ISO8601 but got $x")
    }
  }
}

object DateTimeJsonProtocol extends DateTimeJsonProtocol
