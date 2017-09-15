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

import scala.concurrent.{ExecutionContext, Future}
import scalaz.std.scalaFuture._

import com.google.inject.Inject
import com.google.inject.name.Named
import shapeless.HNil
import spray.http.StatusCodes
import spray.routing._

import ai.deepsense.commons.json.IdJsonProtocol
import ai.deepsense.commons.json.envelope.{Envelope, EnvelopeJsonFormat}
import ai.deepsense.commons.models.Id
import ai.deepsense.commons.rest.{Cors, RestComponent}
import ai.deepsense.commons.utils.Logging
import ai.deepsense.sessionmanager.rest.requests.CreateSession
import ai.deepsense.sessionmanager.rest.responses.ListSessionsResponse
import ai.deepsense.sessionmanager.service.sessionspawner.SessionConfig
import ai.deepsense.sessionmanager.service.{Session, SessionService, UnauthorizedOperationException}

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
        handleRejections(rejectionHandler) {
          handleExceptions(exceptionHandler) {
            waitForHeartbeat {
              pathPrefix(sessionsPathPrefixMatcher) {
                pathPrefix(JavaUUID) { sessionId =>
                  pathPrefix("status") {
                    pathEndOrSingleSlash {
                      get {
                        complete {
                          sessionService.nodeStatusesQuery(userId, sessionId).run
                        }
                      }}} ~
                    pathEndOrSingleSlash {
                      get {
                        complete {
                          val session = sessionService.getSession(userId, sessionId)
                          session.map(Envelope[Session]).run
                        }
                      } ~
                        post {
                          complete {
                            sessionService.launchSession(userId, sessionId).run.map { _ => StatusCodes.OK }
                          }
                        } ~
                        delete {
                          complete {
                            sessionService.killSession(userId, sessionId).run.map { _ => StatusCodes.OK }
                          }
                        }
                    }
                } ~
                  pathEndOrSingleSlash {
                    post {
                      entity(as[CreateSession]) { request =>
                        val sessionConfig = SessionConfig(request.workflowId, userId)
                        val clusterDetails = request.cluster
                        val session = sessionService.createSession(sessionConfig, clusterDetails)
                        val enveloped = session.map(Envelope(_))
                        complete(enveloped)
                      }
                    } ~
                      get {
                        complete(sessionService.listSessions(userId).map(ListSessionsResponse))
                      }
                  }
              }
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

  val rejectionHandler: RejectionHandler = RejectionHandler {
    case NotSubscribedToHeartbeatsRejection :: _ =>
      complete {
        logger.warn("Rejected a request because not yet subscribed to Heartbeats!")
        (StatusCodes.ServiceUnavailable, "Session Manager is starting!")
      }
  }

  val exceptionHandler: ExceptionHandler = ExceptionHandler {
    case t: IllegalArgumentException =>
      complete {
        (StatusCodes.BadRequest, t.getMessage)
      }
    case t: UnauthorizedOperationException =>
      complete {
        (StatusCodes.Forbidden, t.getMessage)
      }
  }

  private def waitForHeartbeat: Directive0 = {
    extract(_ => heartbeatSubscribed.isCompleted)
      .flatMap[HNil](if (_) pass else reject(NotSubscribedToHeartbeatsRejection)) &
      cancelRejection(NotSubscribedToHeartbeatsRejection)
  }

  case object NotSubscribedToHeartbeatsRejection extends Rejection
}
