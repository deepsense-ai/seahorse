/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.workflowexecutor

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Try

import akka.actor.ActorSystem
import akka.io.IO
import akka.pattern.ask
import spray.can.Http
import spray.client.pipelining._
import spray.http._
import spray.util._

import ai.deepsense.commons.utils.Logging
import ai.deepsense.sparkutils
import ai.deepsense.workflowexecutor.exception.UnexpectedHttpResponseException

class WorkflowDownloadClient(
    val address: String,
    val path: String,
    val timeout: Int)
  extends Logging {

  val downloadUrl = (workflowId: String) =>
    s"$address/$path/$workflowId/download"

  def downloadWorkflow(workflowId: String): Future[String] = {

    logger.info(s"Downloading workflow $workflowId...")

    implicit val system = ActorSystem()
    import system.dispatcher
    implicit val timeoutSeconds = timeout.seconds

    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
    val futureResponse = pipeline(Get(downloadUrl(workflowId)))

    futureResponse.onComplete { _ =>
      Try(IO(Http).ask(Http.CloseAll)(1.second).await)
      sparkutils.AkkaUtils.terminate(system)
    }
    futureResponse.map(handleResponse)
  }

  private def handleResponse(response: HttpResponse): String = {
    response.status match {
      case StatusCodes.OK =>
        response.entity.data.asString
      case _ => throw UnexpectedHttpResponseException(
        "Workflow download failed", response.status, response.entity.data.asString)
    }
  }
}
