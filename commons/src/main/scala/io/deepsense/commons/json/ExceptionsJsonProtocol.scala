/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.commons.json

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.commons.exception.{ExceptionDetails, FailureDescription}

trait ExceptionsJsonProtocol
  extends DefaultJsonProtocol
  with SprayJsonSupport {

  implicit val restExceptionJsonWriter = jsonFormat4(FailureDescription.apply)

  implicit object ExceptionDetailsWriter extends JsonWriter[ExceptionDetails] {
    override def write(obj: ExceptionDetails): JsValue = {
      JsObject() // TODO Define and implement exceptions details.
    }
  }
}

object ExceptionsJsonProtocol extends ExceptionsJsonProtocol
