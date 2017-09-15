/**
 * Copyright 2015, CodiLime Inc.
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

package io.deepsense.commons.json

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.commons.exception.FailureCode.FailureCode
import io.deepsense.commons.exception.{FailureCode, FailureDescription, IllegalDeepSenseArgumentException}

trait ExceptionsJsonProtocol
  extends DefaultJsonProtocol
  with IdJsonProtocol
  with SprayJsonSupport {

  implicit object FailureCodeJsonFormat extends JsonFormat[FailureCode] with SprayJsonSupport {
    override def write(code: FailureCode): JsValue = JsNumber(code.id)

    override def read(json: JsValue): FailureCode = json match {
      case JsNumber(code) => FailureCode.fromCode(code.toInt)
        .getOrElse(throw new IllegalDeepSenseArgumentException((s"Unknown FailureCode: $code")))
      case x => deserializationError(s"Expected code as JsNumber, but got $x")
    }
  }

  implicit val failureDescriptionJsonProtocol = jsonFormat5(FailureDescription.apply)
}

object ExceptionsJsonProtocol extends ExceptionsJsonProtocol
