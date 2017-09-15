/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

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

import io.deepsense.commons.models.{ClusterDetails, Id}
import io.deepsense.commons.utils.Logging
import io.deepsense.sparkutils.ScalaUtils
import io.deepsense.sessionmanager.rest.responses.NodeStatusesResponse
import io.deepsense.sessionmanager.service.SessionService.FutureOpt
import io.deepsense.sessionmanager.service.actors.SessionServiceActor
import io.deepsense.sessionmanager.service.sessionspawner.{ExecutorSession, SessionConfig}

class SessionService @Inject() (
  @Named("SessionService.Actor") private val serviceActor: ActorRef,
  @Named("session-service.timeout") private val timeout: Int,
  @Named("predefined-users.admin.id") private val adminUserId: String
)(implicit ec: ExecutionContext) extends Logging {

  private implicit val implicitTimeout = Timeout(timeout, TimeUnit.MILLISECONDS)

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
