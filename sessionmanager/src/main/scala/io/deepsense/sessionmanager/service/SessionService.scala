/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

import java.util.concurrent.TimeUnit

import scala.concurrent.{ExecutionContext, Future}

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.Inject
import com.google.inject.name.Named

import io.deepsense.commons.models.Id
import io.deepsense.commons.utils.Logging
import io.deepsense.sessionmanager.rest.responses.ListSessionsResponse
import io.deepsense.sessionmanager.service.actors.SessionServiceActor

class SessionService @Inject() (
  @Named("SessionService.Actor") private val serviceActor: ActorRef,
  @Named("session-service.timeout") private val timeout: Int
)(implicit ec: ExecutionContext) extends Logging {

  private implicit val implicitTimeout = Timeout(timeout, TimeUnit.MILLISECONDS)

  def getSession(workflowId: Id): Future[Option[Session]] = {
    logger.info(s"Getting session '$workflowId'")
    (serviceActor ? SessionServiceActor.GetRequest(workflowId)).mapTo[Option[Session]]
  }

  def createSession(workflowId: Id, userId: String, token: String): Future[Id] = {
    logger.info(s"Creating session '$workflowId'")
    (serviceActor ? SessionServiceActor.CreateRequest(workflowId, userId, token)).mapTo[Id]
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
}
