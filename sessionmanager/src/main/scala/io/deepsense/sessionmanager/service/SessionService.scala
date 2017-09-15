/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

import java.util.concurrent.TimeUnit

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.Inject
import com.google.inject.name.Named

import io.deepsense.commons.models.Id
import io.deepsense.commons.models.ClusterDetails
import io.deepsense.commons.utils.Logging
import io.deepsense.models.workflows.ExecutionReport
import io.deepsense.sessionmanager.rest.responses.{ListSessionsResponse, NodeStatusesResponse}
import io.deepsense.sessionmanager.service.actors.SessionServiceActor
import io.deepsense.sessionmanager.service.sessionspawner.SessionConfig

class SessionService @Inject() (
  @Named("SessionService.Actor") private val serviceActor: ActorRef,
  @Named("session-service.timeout") private val timeout: Int
)(implicit ec: ExecutionContext) extends Logging {

  private implicit val implicitTimeout = Timeout(timeout, TimeUnit.MILLISECONDS)

  def getSession(workflowId: Id): Future[Option[Session]] = {
    logger.info(s"Getting session '$workflowId'")
    (serviceActor ? SessionServiceActor.GetRequest(workflowId)).mapTo[Option[Session]]
  }

  def createSession(
      sessionConfig: SessionConfig,
      clusterConfig: ClusterDetails): Future[Session] = {
    logger.info(s"Creating session with config $sessionConfig")
    (serviceActor ? SessionServiceActor.CreateRequest(sessionConfig, clusterConfig)).mapTo[Session]
  }

  def listSessions(): Future[ListSessionsResponse] = {
    logger.info(s"Listing sessions")
    (serviceActor ? SessionServiceActor.ListRequest())
      .mapTo[List[Session]]
      .map(ListSessionsResponse)
  }

  def killSession(workflowId: Id): Future[Unit] = {
    logger.info(s"Killing session '$workflowId'")
    (serviceActor ? SessionServiceActor.KillRequest(workflowId)).mapTo[Unit]
  }

  def launchSession(workflowId: Id): Future[Unit] = {
    logger.info(s"Launching nodes in session '$workflowId'")
    (serviceActor ? SessionServiceActor.LaunchRequest(workflowId)).mapTo[Try[Unit]].flatMap(Future.fromTry)
  }

  def nodeStatusesQuery(workflowId: Id): Future[NodeStatusesResponse] = {
    logger.info(s"Asking for node statuses for session $workflowId")
    (serviceActor ? SessionServiceActor.NodeStatusesRequest(workflowId))
      .mapTo[Try[NodeStatusesResponse]]
      .flatMap(Future.fromTry)
  }
}
