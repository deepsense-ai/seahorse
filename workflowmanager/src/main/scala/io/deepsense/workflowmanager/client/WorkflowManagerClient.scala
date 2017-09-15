/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.workflowmanager.client

import java.net.URL
import java.util.UUID

import akka.actor.ActorSystem
import akka.util.Timeout
import spray.client.pipelining._
import spray.http._
import spray.json.RootJsonFormat

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

import io.deepsense.commons.json.envelope.{Envelope, EnvelopeJsonFormat}
import io.deepsense.commons.rest.client.RestClient
import io.deepsense.commons.utils.Logging
import io.deepsense.models.json.workflow.WorkflowInfoJsonProtocol
import io.deepsense.models.workflows.{Workflow, WorkflowInfo}
import io.deepsense.workflowmanager.model.{WorkflowDescription, WorkflowDescriptionJsonProtocol}

class WorkflowManagerClient(
    override val apiUrl: URL,
    mandatoryUserId: UUID,
    mandatoryUserName: String,
    override val credentials: Option[HttpCredentials])(
    implicit override val as: ActorSystem,
    override val timeout: Timeout)
  extends RestClient with WorkflowInfoJsonProtocol
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
    (implicit rootJsonWorklow: RootJsonFormat[Workflow]): Future[Workflow.Id] = {
    uploadWorkflow(rootJsonWorklow.write(workflow).toString())
  }

  def uploadWorkflow(workflow: String): Future[Workflow.Id] = {
    fetchResponse[Envelope[Workflow.Id]](Post(
      endpointPath(s"upload"),
      MultipartFormData(Seq(BodyPart(HttpEntity(workflow), "workflowFile"))))).map(_.content)
  }

}
