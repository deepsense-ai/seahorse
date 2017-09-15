/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.rest

import javax.inject.Named

import com.google.inject.Inject
import io.deepsense.commons.json.IdJsonProtocol
import io.deepsense.commons.json.envelope.{Envelope, EnvelopeJsonFormat}
import io.deepsense.commons.models.Id
import io.deepsense.commons.rest.{Cors, RestComponent}
import io.deepsense.sessionmanager.rest.requests.CreateSession
import io.deepsense.sessionmanager.service.{Session, SessionService}
import spray.http.StatusCodes
import spray.routing._

import scala.concurrent.ExecutionContext

class SessionsApi @Inject() (
  @Named("session-api.prefix") private val sessionsApiPrefix: String,
  private val sessionService: SessionService
)(implicit ec: ExecutionContext) extends RestComponent
  with Directives
  with Cors
  with SessionsJsonProtocol
  with IdJsonProtocol {

  private val sessionsPathPrefixMatcher = PathMatchers.separateOnSlashes(sessionsApiPrefix)
  private val SeahorseUserIdHeaderName = "X-Seahorse-UserId".toLowerCase

  implicit val envelopedSessionFormat = new EnvelopeJsonFormat[Session]("session")
  implicit val envelopedSessionIdFormat = new EnvelopeJsonFormat[Id]("sessionId")

  override def route: Route = {
    cors {
      withUserId { userId =>  // We expect header with userId for all requests
        path("") {
          get {
            complete("Session Manager")
          }
        } ~
        pathPrefix(sessionsPathPrefixMatcher) {
          path(JavaUUID) { sessionId =>
            get {
              complete {
                val session = sessionService.getSession(sessionId)
                session.map(_.map(Envelope(_)))
              }
            } ~
            delete {
              onSuccess(sessionService.killSession(sessionId)) { _ =>
                complete(StatusCodes.OK)
              }
            }
          } ~
          pathEndOrSingleSlash {
            post {
              entity(as[CreateSession]) { request =>
                val session = sessionService.createSession(request.workflowId, userId)
                val enveloped = session.map(Envelope(_))
                complete(enveloped)
              }
            } ~
            get {
              complete(sessionService.listSessions())
            }
          }
        }
      }
    }
  }

  private def withUserId: Directive1[String] = {
    optionalHeaderValueByName(SeahorseUserIdHeaderName).flatMap {
      case Some(value) => provide(value)
      case None => complete(StatusCodes.BadRequest)
    }
  }
}
