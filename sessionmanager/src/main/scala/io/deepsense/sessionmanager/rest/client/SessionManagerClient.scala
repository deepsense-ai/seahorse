/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.rest.client

import java.net.URL
import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}
import akka.actor.ActorSystem
import akka.util.Timeout
import spray.client.pipelining._
import spray.http.{HttpCredentials, HttpResponse}

import io.deepsense.commons.json.envelope.{Envelope, EnvelopeJsonFormat}
import io.deepsense.commons.models.ClusterDetails
import io.deepsense.commons.rest.client.RestClient
import io.deepsense.models.workflows.Workflow
import io.deepsense.sessionmanager.rest.SessionsJsonProtocol
import io.deepsense.sessionmanager.rest.requests.CreateSession
import io.deepsense.sessionmanager.rest.responses.{ListSessionsResponse, NodeStatusesResponse}
import io.deepsense.sessionmanager.service.Session

class SessionManagerClient(
    override val apiUrl: URL,
    mandatoryUserId: UUID,
    mandatoryUserName: String,
    override val credentials: Option[HttpCredentials]) (
    implicit override val as: ActorSystem,
    override val timeout: Timeout) extends RestClient with SessionsJsonProtocol {

  override def userId: Option[UUID] = Some(mandatoryUserId)
  override def userName: Option[String] = Some(mandatoryUserName)

  implicit val envelopeFormat = new EnvelopeJsonFormat[Session]("session")

  def fetchSessions(): Future[Traversable[Session]] = {
    fetchResponse[ListSessionsResponse](Get(endpointPath(""))).map(_.sessions)
  }

  def fetchSession(workflowId: Workflow.Id): Future[Session] = {
    fetchResponse[Envelope[Session]](Get(endpointPath(workflowId.toString))).map(_.content)
  }

  def createSession(workflowId: Workflow.Id, cluster: ClusterDetails): Future[Session] = {
    fetchResponse[Envelope[Session]](Post(endpointPath(""), CreateSession(workflowId, cluster))).map(_.content)
  }

  def deleteSession(workflowId: Workflow.Id): Future[HttpResponse] = {
    fetchHttpResponse(Delete(endpointPath(workflowId.toString)))
  }

  def launchSession(workflowId: Workflow.Id): Future[HttpResponse] = {
    fetchHttpResponse(Post(endpointPath(workflowId.toString)))
  }

  def queryNodeStatuses(workflowId: Workflow.Id): Future[NodeStatusesResponse] = {
    fetchResponse[NodeStatusesResponse](Get(endpointPath(s"${workflowId.toString}/status")))
  }
}
