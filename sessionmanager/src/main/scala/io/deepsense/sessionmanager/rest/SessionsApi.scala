/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.rest

import scala.concurrent.ExecutionContext

import spray.routing.{Directives, PathMatchers, Route}

import io.deepsense.commons.rest.RestComponent
import io.deepsense.sessionmanager.service.SessionService

class SessionsApi(
  private val sessionsApiPrefix: String,
  private val sessionService: SessionService
)(implicit ec: ExecutionContext) extends RestComponent
  with Directives
  with SessionsJsonProtocol {

  private val sessionsPathPrefixMatcher = PathMatchers.separateOnSlashes(sessionsApiPrefix)

  override def route: Route = { // TODO Cors
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
