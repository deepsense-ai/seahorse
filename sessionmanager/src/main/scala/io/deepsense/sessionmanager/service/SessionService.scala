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
import io.deepsense.sessionmanager.rest.responses.ListSessionsResponse
import io.deepsense.sessionmanager.service.SessionServiceActor.KilledResponse

class SessionService @Inject() (
  @Named("SessionService.Actor") private val serviceActor: ActorRef,
  @Named("session-service.timeout") private val timeout: Int
)(implicit ec: ExecutionContext) {

  private implicit val implicitTimeout = Timeout(timeout, TimeUnit.MILLISECONDS)

  def getSession(workflowId: Id): Future[Option[Session]] = {
    (serviceActor ? SessionServiceActor.GetRequest(workflowId)).mapTo[Option[Session]]
  }

  def createSession(workflowId: Id): Future[Session] = {
    (serviceActor ? SessionServiceActor.CreateRequest(workflowId)).mapTo[Session]
  }

  def listSessions(): Future[ListSessionsResponse] = {
    (serviceActor ? SessionServiceActor.ListRequest())
      .mapTo[List[Session]]
      .map(ListSessionsResponse)
  }

  def killSession(workflowId: Id): Future[KilledResponse] = {
    (serviceActor ? SessionServiceActor.KillRequest(workflowId)).mapTo[KilledResponse]
  }
}
