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

package ai.deepsense.sessionmanager.service

import java.util.concurrent.TimeUnit

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scalaz.ListT._
import scalaz.OptionT
import scalaz.OptionT._
import scalaz.std.scalaFuture._

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.Inject
import com.google.inject.name.Named

import ai.deepsense.commons.models.{ClusterDetails, Id}
import ai.deepsense.commons.utils.Logging
import ai.deepsense.sparkutils.ScalaUtils
import ai.deepsense.sessionmanager.rest.responses.NodeStatusesResponse
import ai.deepsense.sessionmanager.service.SessionService.FutureOpt
import ai.deepsense.sessionmanager.service.actors.SessionServiceActor
import ai.deepsense.sessionmanager.service.sessionspawner.{ExecutorSession, SessionConfig}

class SessionService @Inject() (
  @Named("SessionService.Actor") private val serviceActor: ActorRef,
  @Named("session-service.timeout") private val timeoutMillis: Int,
  @Named("predefined-users.admin.id") private val adminUserId: String
)(implicit ec: ExecutionContext) extends Logging {

  private implicit val implicitTimeout = Timeout(timeoutMillis, TimeUnit.MILLISECONDS)

  def getSession(callerId: String, workflowId: Id): FutureOpt[Session] = {
    logger.info(s"Getting session '$workflowId'")

    getSessionImpl(callerId, workflowId)
      .map { _.sessionForApi() }
  }

  def createSession(
      sessionConfig: SessionConfig,
      clusterConfig: ClusterDetails): Future[Session] = {
    logger.info(s"Creating session with config $sessionConfig")
    (serviceActor ? SessionServiceActor.CreateRequest(sessionConfig, clusterConfig)).mapTo[Session]
  }

  def listSessions(callerId: String): Future[List[Session]] = {
    logger.info(s"Listing sessions")
    listT((serviceActor ? SessionServiceActor.ListRequest()).mapTo[List[ExecutorSession]])
      .filter(session => isAuthorized(callerId = callerId, ownerId = session.sessionConfig.userId))
      .map { _.sessionForApi() }
      .run
  }

  def killSession(callerId: String, workflowId: Id): FutureOpt[Unit] =
    getSessionImpl(callerId, workflowId)
      .map { _ =>
        logger.info(s"Killing session '$workflowId'")
        (serviceActor ? SessionServiceActor.KillRequest(workflowId)).mapTo[Unit]
      }

  def launchSession(callerId: String, workflowId: Id): FutureOpt[Unit] =
    getSessionImpl(callerId, workflowId)
      .map { _ =>
        logger.info(s"Launching nodes in session '$workflowId'")
        (serviceActor ? SessionServiceActor.LaunchRequest(workflowId))
          .mapTo[Try[Unit]]
          .flatMap(ScalaUtils.futureFromTry)
      }

  def nodeStatusesQuery(callerId: String, workflowId: Id): FutureOpt[NodeStatusesResponse] =
    getSessionImpl(callerId, workflowId)
      .flatMapF { _ =>
        logger.info(s"Asking for node statuses for session $workflowId")
        (serviceActor ? SessionServiceActor.NodeStatusesRequest(workflowId))
          .mapTo[Try[NodeStatusesResponse]]
          .flatMap(ScalaUtils.futureFromTry)
      }

  private def isAuthorized(callerId: String, ownerId: String): Boolean =
    callerId == ownerId || callerId == adminUserId

  private def getSessionImpl(callerId: String, workflowId: Id): FutureOpt[ExecutorSession] =
    optionT((serviceActor ? SessionServiceActor.GetRequest(workflowId))
      .mapTo[Option[ExecutorSession]])
      .map { es =>
          if (isAuthorized(callerId = callerId, ownerId = es.sessionConfig.userId)) {
            es
          } else {
            throw UnauthorizedOperationException(
              s"user $callerId accessing session '$workflowId' owned by ${es.sessionConfig.userId}")
          }
      }
}

object SessionService {
  type FutureOpt[A] = OptionT[Future, A]
}
