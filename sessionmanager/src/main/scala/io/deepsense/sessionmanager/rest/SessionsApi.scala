/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.rest

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject
import com.google.inject.name.Named
import shapeless.HNil
import spray.http.StatusCodes
import spray.routing._

import io.deepsense.commons.json.IdJsonProtocol
import io.deepsense.commons.json.envelope.{Envelope, EnvelopeJsonFormat}
import io.deepsense.commons.models.Id
import io.deepsense.commons.rest.{Cors, RestComponent}
import io.deepsense.commons.utils.Logging
import io.deepsense.sessionmanager.rest.requests.CreateSession
import io.deepsense.sessionmanager.service.{Session, SessionService}

class SessionsApi @Inject()(
  @Named("session-api.prefix") private val sessionsApiPrefix: String,
  @Named("SessionService.HeartbeatSubscribed") private val heartbeatSubscribed: Future[Unit],
  private val sessionService: SessionService
)(implicit ec: ExecutionContext)
  extends RestComponent
  with Directives
  with Cors
  with SessionsJsonProtocol
  with IdJsonProtocol
  with Logging {

  private val sessionsPathPrefixMatcher = PathMatchers.separateOnSlashes(sessionsApiPrefix)

  implicit val envelopedSessionFormat = new EnvelopeJsonFormat[Session]("session")
  implicit val envelopedSessionIdFormat = new EnvelopeJsonFormat[Id]("sessionId")

  override def route: Route = {
    cors {
      path("") {
        get {
          complete("Session Manager")
        }
      } ~
      handleRejections(rejectionHandler) {
        waitForHeartbeat {
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
                  val session = sessionService.createSession(request.workflowId)
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
  }

  val rejectionHandler: RejectionHandler = RejectionHandler {
    case NotSubscribedToHeartbeatsRejection :: _ =>
      complete {
        logger.warn("Rejected a request because not yet subscribed to Heartbeats!")
        (StatusCodes.ServiceUnavailable, "Session Manager is starting!")
      }
  }.orElse(RejectionHandler.Default)

  private def waitForHeartbeat: Directive0 = {
    extract(_ => heartbeatSubscribed.isCompleted)
      .flatMap[HNil](if (_) pass else reject(NotSubscribedToHeartbeatsRejection)) &
      cancelRejection(NotSubscribedToHeartbeatsRejection)
  }

  case object NotSubscribedToHeartbeatsRejection extends Rejection
}
