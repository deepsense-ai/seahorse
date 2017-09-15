/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.workflowmanager.client

import java.net.URL

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

import akka.actor.ActorSystem
import akka.util.Timeout
import spray.client.pipelining._
import spray.http._
import spray.json.RootJsonFormat

import io.deepsense.commons.json.envelope.{Envelope, EnvelopeJsonFormat}
import io.deepsense.commons.rest.client.RestClient
import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow.{WorkflowInfoJsonProtocol, WorkflowJsonProtocol}
import io.deepsense.models.workflows.{Workflow, WorkflowInfo}
import io.deepsense.workflowmanager.model.{WorkflowDescription, WorkflowDescriptionJsonProtocol}

class WorkflowManagerClient(val workflowsUrl: URL,
    override val userId: Option[String],
    override val userName: Option[String],
    override val credentials: Option[HttpCredentials])(
    implicit override val ctx: ExecutionContext,
    override val as: ActorSystem,
    override val timeout: Timeout)
  extends RestClient with WorkflowInfoJsonProtocol
    with WorkflowDescriptionJsonProtocol
    with Logging {

  override val apiUrl: URL = workflowsUrl

  implicit private val envelopeWorkflowIdJsonFormat =
    new EnvelopeJsonFormat[Workflow.Id]("workflowId")


  def fetchWorkflows(): Future[List[WorkflowInfo]] = {
    fetchResponse[List[WorkflowInfo]](Get(endpointPath("")))
  }

  def fetchWorkflow(id: Workflow.Id)(implicit workflowJsonFormat: RootJsonFormat[Workflow]): Future[Workflow] = {
    fetchResponse[Workflow](Get(endpointPath(s"$id")))
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
