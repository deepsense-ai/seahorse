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

package ai.deepsense.workflowmanager.client

import java.net.URL
import java.util.UUID

import scala.concurrent.Future
import scala.language.postfixOps

import akka.actor.ActorSystem
import akka.util.Timeout
import spray.client.pipelining._
import spray.http._
import spray.json.RootJsonFormat

import ai.deepsense.commons.json.envelope.{Envelope, EnvelopeJsonFormat}
import ai.deepsense.commons.rest.client.RestClient
import ai.deepsense.commons.utils.Logging
import ai.deepsense.models.json.workflow.WorkflowInfoJsonProtocol
import ai.deepsense.models.workflows.{Workflow, WorkflowInfo, WorkflowWithVariables}
import ai.deepsense.workflowmanager.model.{WorkflowDescription, WorkflowDescriptionJsonProtocol}

class WorkflowManagerClient(
    override val apiUrl: URL,
    mandatoryUserId: UUID,
    mandatoryUserName: String,
    override val credentials: Option[HttpCredentials])(
    implicit override val as: ActorSystem,
    override val timeout: Timeout)
  extends RestClient
  with WorkflowInfoJsonProtocol
  with WorkflowDescriptionJsonProtocol
  with Logging {

  override def userId: Option[UUID] = Some(mandatoryUserId)
  override def userName: Option[String] = Some(mandatoryUserName)

  implicit private val envelopeWorkflowIdJsonFormat =
    new EnvelopeJsonFormat[Workflow.Id]("workflowId")


  def fetchWorkflows(): Future[List[WorkflowInfo]] = {
    fetchResponse[List[WorkflowInfo]](Get(endpointPath("")))
  }

  def fetchWorkflow(id: Workflow.Id)(implicit workflowJsonFormat: RootJsonFormat[Workflow]): Future[Workflow] = {
    fetchResponse[Workflow](Get(endpointPath(s"$id")))
  }

  def fetchWorkflowInfo(id: Workflow.Id): Future[WorkflowInfo] = {
    fetchResponse[WorkflowInfo](Get(endpointPath(s"$id/info")))
  }

  def cloneWorkflow(workflowId: Workflow.Id,
      workflowDescription: WorkflowDescription):
  Future[Workflow.Id] = {
    fetchResponse[Envelope[Workflow.Id]](Post(
      endpointPath(s"$workflowId/clone"),
      workflowDescription
    )).map(_.content)
  }

  def deleteWorkflow(workflowId: Workflow.Id): Future[Unit] = {
    fetchHttpResponse(Delete(endpointPath(s"$workflowId"))).map(_ => ())
  }

  def uploadWorkflow(workflow: Workflow)
    (implicit rootJsonWorkflow: RootJsonFormat[Workflow]): Future[Workflow.Id] = {
    uploadWorkflow(rootJsonWorkflow.write(workflow).toString())
  }

  def downloadWorkflow(workflowId: Workflow.Id)
    (implicit jsonFormat: RootJsonFormat[WorkflowWithVariables]): Future[Option[WorkflowWithVariables]] = {
    fetchResponse[Option[WorkflowWithVariables]](Get(endpointPath(s"$workflowId/download?export-datasources=true")))
  }

  def uploadWorkflow(workflow: String): Future[Workflow.Id] = {
    fetchResponse[Envelope[Workflow.Id]](Post(
      endpointPath(s"upload"),
      MultipartFormData(Seq(BodyPart(HttpEntity(workflow), "workflowFile"))))).map(_.content)
  }
}
