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

import spray.json._

/**
 * Utility that produces JsonFormat for enumeration.
 */
object EnumerationSerializer {
  def jsonEnumFormat[T <: Enumeration](enumeration: T): JsonFormat[T#Value] = {
    new JsonFormat[T#Value] {
      def write(obj: T#Value) = JsString(obj.toString)
      def read(json: JsValue) = json match {
        case JsString(txt) => enumeration.withName(txt)
        case x => throw new DeserializationException(
          s"Expected a value from enum $enumeration instead of $x")
      }
    }
  }
}
