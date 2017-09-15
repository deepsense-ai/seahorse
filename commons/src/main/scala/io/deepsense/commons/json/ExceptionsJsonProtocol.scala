/**
 * Copyright (c) 2015, CodiLime Inc.
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
