/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.sessionmanager.rest

import spray.httpx.SprayJsonSupport
import spray.json._

import ai.deepsense.commons.json.IdJsonProtocol
import ai.deepsense.commons.rest.ClusterDetailsJsonProtocol
import ai.deepsense.commons.rest.ClusterDetailsJsonProtocol._
import ai.deepsense.models.json.graph.NodeStatusJsonProtocol
import ai.deepsense.sessionmanager.rest.requests.CreateSession
import ai.deepsense.sessionmanager.rest.responses.{ListSessionsResponse, NodeStatusesResponse}
import ai.deepsense.sessionmanager.service.{Session, Status}


trait SessionsJsonProtocol
  extends DefaultJsonProtocol
  with IdJsonProtocol
  with SprayJsonSupport
  with NodeStatusJsonProtocol {

  implicit val statusFormat = new RootJsonFormat[Status.Value] {
    override def read(json: JsValue): Status.Value = json match {
      case JsString(value) =>
        value match {
          case "running" => Status.Running
          case "creating" => Status.Creating
          case "error" => Status.Error
        }
      case x => deserializationError(s"Expected 'status' to be a string but got $x")
    }

    override def write(obj: Status.Value): JsValue = JsString(obj.toString)
  }

  implicit val sessionFormat = jsonFormat3(Session.apply)

  implicit val nodeStatusesResponseFormat = jsonFormat1(NodeStatusesResponse)

  implicit val createSessionFormat = jsonFormat2(CreateSession)

  implicit val listSessionsResponseFormat = new RootJsonFormat[ListSessionsResponse] {
    override def write(obj: ListSessionsResponse): JsValue = {
      JsObject(
        "sessions" -> JsArray(obj.sessions.map(_.toJson): _*)
      )
    }

    override def read(json: JsValue): ListSessionsResponse = {
      val sessions = json.asJsObject().fields.getOrElse(
        "sessions", deserializationError("Expected 'sessions' field!"))
      sessions match {
        case JsArray(elements) => ListSessionsResponse(elements.map(_.convertTo[Session]).toList)
        case x => deserializationError(s"Expected 'sessions' to be an array but got $x")
      }
    }
  }
}

object SessionsJsonProtocol extends SessionsJsonProtocol
