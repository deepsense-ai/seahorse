/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

import scala.concurrent.Future

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import io.deepsense.commons.models.Id
import io.deepsense.sessionmanager.service.SessionServiceActor.KillResponse

class SessionService(
  private val serviceActor: ActorRef,
  private implicit val timeout: Timeout
) {

  def getSession(workflowId: Id): Future[Option[Session]] = {
    (serviceActor ? SessionServiceActor.GetRequest(workflowId)).mapTo[Option[Session]]
  }

  def createSession(workflowId: Id): Future[Session] = {
    (serviceActor ? SessionServiceActor.CreateRequest(workflowId)).mapTo[Session]
  }

  def listSessions(): Future[List[Session]] = {
    (serviceActor ? SessionServiceActor.ListRequest()).mapTo[List[Session]]
  }

  def killSession(workflowId: Id): Future[KillResponse] = {
    (serviceActor ? SessionServiceActor.KillRequest(workflowId)).mapTo[KillResponse]
  }
}
