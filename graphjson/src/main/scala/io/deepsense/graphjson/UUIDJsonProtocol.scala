/**
 * Copyright (c) 2015, CodiLime Inc.
 */

// TODO This class is also in experimentmanager package.
// It should be extracted to some common package
package io.deepsense.graphjson

import java.util.UUID

import spray.json._

trait UUIDJsonProtocol {

  implicit object UUIDFormat extends JsonFormat[UUID] {
    override def write(obj: UUID) = JsString(obj.toString)

    override def read(json: JsValue): UUID = json match {
      case JsString(x) => UUID.fromString(x)
      case x => deserializationError(s"Expected UUID as JsString, but got $x")
    }
  }
}

object UUIDJsonProtocol extends UUIDJsonProtocol
