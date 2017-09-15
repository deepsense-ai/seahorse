/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.commons.json

import java.util.UUID

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.commons.models.Id

trait IdJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit object IdFormat extends RootJsonFormat[Id] {
    override def write(obj: Id) = JsString(obj.value.toString)
    override def read(json: JsValue): Id = json match {
      case JsString(x) => Id(UUID.fromString(x))
      case x => deserializationError(s"Expected Id as UUID in JsString, but got $x")
    }
  }
}

object IdJsonProtocol extends IdJsonProtocol
