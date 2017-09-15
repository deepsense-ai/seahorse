/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.rest

import javax.inject.Named

import scala.concurrent.ExecutionContext

import com.google.inject.Inject
import spray.routing.{Directives, PathMatchers, Route}

import io.deepsense.commons.rest.{Cors, RestComponent}
import io.deepsense.sessionmanager.service.SessionService

class SessionsApi @Inject() (
  @Named("session-api.prefix") private val sessionsApiPrefix: String,
  private val sessionService: SessionService
)(implicit ec: ExecutionContext) extends RestComponent
  with Directives
  with Cors
  with SessionsJsonProtocol {

  private val sessionsPathPrefixMatcher = PathMatchers.separateOnSlashes(sessionsApiPrefix)

  override def route: Route = {
    cors {
      path("") {
        get {
          complete("Session Manager")
        }
      } ~
      pathPrefix(sessionsPathPrefixMatcher) {
        path(JavaUUID) { sessionId =>
          get {
            complete(sessionService.getSession(sessionId))
          } ~
          delete {
            complete(sessionService.killSession(sessionId))
          } ~
          post {
            complete(sessionService.createSession(sessionId))
          }
        } ~
        get {
          complete(sessionService.listSessions())
        }
      }
    }
  }
}
