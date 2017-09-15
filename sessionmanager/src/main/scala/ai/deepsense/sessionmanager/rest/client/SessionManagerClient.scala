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

package ai.deepsense.sessionmanager.rest.client

import java.net.URL
import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}
import akka.actor.ActorSystem
import akka.util.Timeout
import spray.client.pipelining._
import spray.http.{HttpCredentials, HttpResponse}

import ai.deepsense.commons.json.envelope.{Envelope, EnvelopeJsonFormat}
import ai.deepsense.commons.models.ClusterDetails
import ai.deepsense.commons.rest.client.RestClient
import ai.deepsense.models.workflows.Workflow
import ai.deepsense.sessionmanager.rest.SessionsJsonProtocol
import ai.deepsense.sessionmanager.rest.requests.CreateSession
import ai.deepsense.sessionmanager.rest.responses.{ListSessionsResponse, NodeStatusesResponse}
import ai.deepsense.sessionmanager.service.Session

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
