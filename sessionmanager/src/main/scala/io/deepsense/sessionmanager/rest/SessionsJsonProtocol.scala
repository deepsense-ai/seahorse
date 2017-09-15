/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.rest

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.commons.json.IdJsonProtocol
import io.deepsense.sessionmanager.service.Session
import io.deepsense.sessionmanager.service.SessionServiceActor.KillResponse

trait SessionsJsonProtocol
  extends DefaultJsonProtocol
  with IdJsonProtocol
  with SprayJsonSupport {

  implicit val sessionFormat = new RootJsonFormat[Session] {
    override def write(obj: Session): JsValue = {
      JsObject(
        "id" -> obj.handle.workflowId.toJson,
        "status" -> JsString(obj.status.toString)
      )
    }

    override def read(json: JsValue): Session =
      throw new UnsupportedOperationException()
  }

  implicit val killResponseFormat = jsonFormat2(KillResponse)
}

object SessionsJsonProtocol extends SessionsJsonProtocol
