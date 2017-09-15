/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.rest.json

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.experimentmanager.exceptions.ExceptionDetails
import io.deepsense.experimentmanager.rest.RestException

trait ExceptionsJsonProtocol
  extends DefaultJsonProtocol
  with SprayJsonSupport {

  implicit val restExceptionJsonWriter = jsonFormat4(RestException.apply)

  implicit object ExceptionDetailsWriter extends JsonWriter[ExceptionDetails] {
    override def write(obj: ExceptionDetails): JsValue = {
      JsObject() // TODO Define and implement exceptions details.
    }
  }
}

object ExceptionsJsonProtocol extends ExceptionsJsonProtocol
