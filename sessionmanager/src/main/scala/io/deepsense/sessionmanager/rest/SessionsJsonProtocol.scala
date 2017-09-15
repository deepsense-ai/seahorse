/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.rest

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.commons.json.IdJsonProtocol
import io.deepsense.sessionmanager.rest.requests.CreateSession
import io.deepsense.sessionmanager.rest.responses.ListSessionsResponse
import io.deepsense.sessionmanager.service.Session
import io.deepsense.sessionmanager.service.SessionServiceActor.KilledResponse

trait SessionsJsonProtocol
  extends DefaultJsonProtocol
  with IdJsonProtocol
  with SprayJsonSupport {

  implicit val sessionFormat = new RootJsonFormat[Session] {
    override def write(obj: Session): JsValue = {
      JsObject(
        "workflowId" -> obj.workflowId.toJson,
        "sessionId" -> obj.workflowId.toJson,
        "status" -> JsString(obj.status.toString)
      )
    }

    override def read(json: JsValue): Session =
      throw new UnsupportedOperationException()
  }
  implicit val killResponseFormat = jsonFormat0(KilledResponse)
  implicit val createSessionFormat = jsonFormat1(CreateSession)

  implicit val listSessionsResponseFormat = new RootJsonFormat[ListSessionsResponse] {
    override def write(obj: ListSessionsResponse): JsValue = {
      JsObject(
        "sessions" -> JsArray(obj.sessions.map(_.toJson): _*)
      )
    }

    override def read(json: JsValue): ListSessionsResponse =
      throw new UnsupportedOperationException()
  }
}

object SessionsJsonProtocol extends SessionsJsonProtocol
