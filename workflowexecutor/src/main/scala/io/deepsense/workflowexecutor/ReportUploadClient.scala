/**
 * Copyright 2015, CodiLime Inc.
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

package io.deepsense.workflowexecutor

import scala.concurrent.Future
import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.io.IO
import akka.pattern.ask
import spray.can.Http
import spray.client.pipelining._
import spray.http._
import spray.json._
import spray.util._

import io.deepsense.commons.utils.Logging
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow._
import io.deepsense.models.workflows._
import io.deepsense.workflowexecutor.exception.UnexpectedHttpResponseException

class ReportUploadClient(
    val reportUploadHost: String,
    val reportUploadScheme: String,
    val reportUploadPort: Int,
    val reportUploadPath: String,
    val reportUploadTimeout: Int,
    val reportPreviewScheme: String,
    val reportPreviewPort: Int,
    val reportPreviewPath: String,
    override val graphReader: GraphReader)
  extends Logging
  with WorkflowWithSavedResultsJsonProtocol {

  val uploadUrl =
    s"${reportUploadScheme}://${reportUploadHost}:${reportUploadPort}/${reportUploadPath}"

  val reportUrl = (reportId: String) =>
    s"${reportPreviewScheme}://${reportUploadHost}:${reportPreviewPort}/" +
      s"${reportPreviewPath}/${reportId}"

  def uploadReport(workflow: WorkflowWithResults): Future[String] = {

    logger.info(s"Uploading execution results report for ${workflow.id} to external host...")

    implicit val system = ActorSystem()
    import system.dispatcher
    implicit val timeout = reportUploadTimeout.seconds

    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
    val requestData = createRequestData(workflow)
    val futureResponse = pipeline(Post(uploadUrl, requestData))

    futureResponse.onComplete { _ =>
      IO(Http).ask(Http.CloseAll)(1.second).await
      system.shutdown()
    }
    futureResponse.map(handleResponse)
  }

  private def createRequestData(report: WorkflowWithResults): MultipartFormData = {
    val contentType = ContentType(MediaTypes.`application/json`)
    val contentTypeHeader = HttpHeaders.`Content-Type`(contentType)
    val contentDispositionHeader = HttpHeaders.`Content-Disposition`(
      "form-data", Map("name" -> "workflowFile", "filename" -> "report.json"))
    val headerSeq = Seq(contentTypeHeader, contentDispositionHeader)
    val httpData = HttpData(report.toJson.toString)
    new MultipartFormData(Seq(new BodyPart(HttpEntity(contentType, httpData), headerSeq)))
  }

  private def handleResponse(response: HttpResponse): String = {
    response.status match {
      case StatusCodes.Created =>
        val content = response.entity.data.asString
        val workflow = content.parseJson.convertTo[WorkflowWithSavedResults]
        reportUrl(workflow.executionReport.id.toString)
      case _ => throw UnexpectedHttpResponseException(
        response.status, response.entity.data.asString)
    }
  }
}
