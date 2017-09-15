/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.workflowmanager.client

import java.net.URL

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

import akka.actor.ActorSystem
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http
import spray.can.Http.HostConnectorInfo
import spray.client.pipelining._
import spray.http._
import spray.httpx.unmarshalling.FromResponseUnmarshaller
import spray.json.RootJsonFormat

import io.deepsense.commons.json.envelope.{Envelope, EnvelopeJsonFormat}
import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow.{WorkflowInfoJsonProtocol, WorkflowJsonProtocol}
import io.deepsense.models.workflows.{Workflow, WorkflowInfo}
import io.deepsense.workflowmanager.model.{WorkflowDescription, WorkflowDescriptionJsonProtocol}

class WorkflowManagerClient(val workflowsUrl: URL)(
    implicit val executionContext: ExecutionContext,
    val actorSystem: ActorSystem,
    val timeout: Timeout)
  extends WorkflowInfoJsonProtocol
    with WorkflowDescriptionJsonProtocol
    with Logging {

  implicit private val envelopeWorkflowIdJsonFormat =
    new EnvelopeJsonFormat[Workflow.Id]("workflowId")

  private def hostConnectorFut(): Future[HostConnectorInfo] = {
    (IO(Http) ? Http.HostConnectorSetup(workflowsUrl.getHost, port = workflowsUrl.getPort)).mapTo[HostConnectorInfo]
  }

  private def sendReceivePipeline() : Future[HttpRequest => Future[HttpResponse]] = {
    for {
      HostConnectorInfo(hostConnector, _) <- hostConnectorFut()
    } yield {
      sendReceive(hostConnector)
    }
  }

  private def unmarshalPipeline[U: FromResponseUnmarshaller](): Future[HttpRequest => Future[U]] = {
    for {
      sr <- sendReceivePipeline()
    } yield {
      sr ~> unmarshal[U]
    }
  }

  def fetchResponse[U : FromResponseUnmarshaller](
      req: HttpRequest
  ): Future[U] = {
    unmarshalPipeline().flatMap(pip => {
      pip(req)
    })
  }

  def fetchHttpResponse(req: HttpRequest) : Future[HttpResponse] = {
    sendReceivePipeline().flatMap{ pip =>
      pip(req)
    }
  }

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
    fetchHttpResponse(Delete(endpointPath(s"/$workflowId"))).map(_ => ())
  }

  def uploadWorkflow(workflow: Workflow)(implicit workflowJsonFormat: RootJsonFormat[Workflow]) :
  Future[Workflow.Id] = {
    uploadWorkflow(workflowJsonFormat.write(workflow).toString())
  }

  def uploadWorkflow(workflow: String): Future[Workflow.Id] = {
    fetchResponse[Envelope[Workflow.Id]](Post(
      endpointPath(s"/upload"),
      MultipartFormData(Seq(BodyPart(HttpEntity(workflow), "workflowFile"))))).map(_.content)
  }

  def endpointPath(workflowsEndpoint: String): String = {
    s"${workflowsUrl.getFile}/$workflowsEndpoint"
  }

}
