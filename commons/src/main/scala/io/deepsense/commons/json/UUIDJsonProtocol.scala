/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.json

import java.util.UUID

import spray.json._
import spray.httpx.SprayJsonSupport

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
