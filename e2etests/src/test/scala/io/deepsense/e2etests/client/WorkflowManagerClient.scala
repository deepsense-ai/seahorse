/**
  * Copyright (c) 2016, CodiLime Inc.
  */

package io.deepsense.e2etests.client

import scala.concurrent.{Await, Future, Promise}
import scala.language.postfixOps

import com.ning.http.client.multipart.ByteArrayPart
import com.ning.http.client.{AsyncCompletionHandler, AsyncHttpClient, Request, Response => AHCResponse}
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.libs.ws.ning.NingWSResponse

import io.deepsense.commons.json.envelope.{Envelope, EnvelopeJsonFormat}
import io.deepsense.commons.utils.Logging
import io.deepsense.e2etests.SeahorseIntegrationTestDSL
import io.deepsense.models.json.workflow.WorkflowInfoJsonProtocol
import io.deepsense.models.workflows.{Workflow, WorkflowInfo}
import io.deepsense.sessionmanager.service.{Status => SessionStatus}
import io.deepsense.workflowmanager.model.{WorkflowDescription, WorkflowDescriptionJsonProtocol}

object WorkflowManagerClient
  extends WorkflowInfoJsonProtocol
    with WorkflowDescriptionJsonProtocol
    with Logging
    with SeahorseIntegrationTestDSL {

  import scala.concurrent.ExecutionContext.Implicits.global
  import spray.json.pimpAny

  implicit private val envelopeWorkflowIdJsonFormat =
    new EnvelopeJsonFormat[Workflow.Id]("workflowId")

  def getWorkflow(id: Workflow.Id) = getWorkflows().find(_.id == id).get

  def getWorkflows(): List[WorkflowInfo] = {
    Await.result(
      httpClient.url(workflowsUrl)
        .get()
        .map { response =>
          val json = SprayJsonFromPlayJson(response.json)
          json.convertTo[List[WorkflowInfo]]
        }, httpTimeout
    )
  }

  def cloneWorkflow(workflowId: Workflow.Id, workflowDescription: WorkflowDescription): Workflow.Id = {
    val clonedWorkflowId = Await.result(httpClient.url(s"$workflowsUrl/$workflowId/clone")
      .post(PlayJsonFromSprayJson(workflowDescription.toJson))
      .map { response =>
        val json = SprayJsonFromPlayJson(response.json)
        json.convertTo[Envelope[Workflow.Id]].content
      }, httpTimeout)
    clonedWorkflowId
  }

  def deleteWorkflow(workflowId: Workflow.Id): Unit =
    Await.result(httpClient.url(s"$workflowsUrl/$workflowId").delete(), httpTimeout)

  case class WorkflowId(workflowId: String)
  implicit val workflowIdFormat = Json.format[WorkflowId]

  def uploadWorkflow(js: String): String = {
    val asyncHttpClient = httpClient.underlying[AsyncHttpClient]
    val postBuilder = asyncHttpClient.preparePost(s"$workflowsUrl/upload")
    val builder = postBuilder.addBodyPart(new ByteArrayPart("workflowFile", js.getBytes))
    val updatedWorkflowId =
      Await.result(httpClientExecute(asyncHttpClient, builder.build())
        .map { response =>
          response.json.as[WorkflowId]
        }, httpTimeout)
    updatedWorkflowId.workflowId
  }

  private def httpClientExecute(asyncHttpClient: AsyncHttpClient,
                                request: Request) : Future[NingWSResponse] = {
    val result = Promise[NingWSResponse]()
    asyncHttpClient.executeRequest(request,
      new AsyncCompletionHandler[AHCResponse]() {
        override def onCompleted(response: AHCResponse) = {
          result.success(NingWSResponse(response))
          response
        }
        override def onThrowable(t: Throwable) = {
          result.failure(t)
        }
      })
    result.future
  }

}
