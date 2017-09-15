/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

import java.util.concurrent.TimeUnit

import scala.concurrent.Future

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.Inject
import com.google.inject.name.Named

import io.deepsense.commons.models.Id
import io.deepsense.sessionmanager.service.SessionServiceActor.KillResponse

class SessionService @Inject() (
  @Named("SessionService.Actor") private val serviceActor: ActorRef,
  @Named("session-service.timeout") private val timeout: Int
) {

  private implicit val implicitTimeout = Timeout(timeout, TimeUnit.MILLISECONDS)

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
