/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.rest.json
import java.util.UUID

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.experimentmanager.app.models.{Experiment, Id}

trait IdJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit object IdFormat extends RootJsonFormat[Id] {
    override def write(obj: Id) = JsString(obj.value.toString)
    override def read(json: JsValue): Experiment.Id = json match {
      case JsString(x) => Id(UUID.fromString(x))
      case x => deserializationError(s"Expected Id as UUID as JsString, but got $x")
    }
  }
}

object IdJsonProtocol extends IdJsonProtocol
