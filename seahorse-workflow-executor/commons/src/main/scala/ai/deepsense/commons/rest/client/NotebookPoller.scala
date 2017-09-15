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

package ai.deepsense.commons.rest.client

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

import akka.actor.ActorSystem
import akka.util.Timeout
import spray.client.pipelining._
import spray.http.StatusCodes

import ai.deepsense.commons.models.Id
import ai.deepsense.commons.utils.Retry
import ai.deepsense.commons.utils.RetryActor.RetriableException

class NotebookPoller private (
    notebookRestClient: NotebookRestClient,
    pollInterval: FiniteDuration,
    retryCountLimit: Int,
    workflowId: Id,
    nodeId: Id,
    endpointPath: String)(
    implicit override val actorSystem: ActorSystem,
    override val timeout: Timeout)
  extends Retry[Array[Byte]] {

  override val retryInterval: FiniteDuration = pollInterval

  override val retryLimit: Int = retryCountLimit

  override val workDescription: Option[String] = Some("notebook data retrieval")

  override def work: Future[Array[Byte]] = {
    implicit val ec: ExecutionContext = actorSystem.dispatcher

    notebookRestClient.fetchHttpResponse(Get(endpointPath)).flatMap { resp =>
      resp.status match {
        case StatusCodes.NotFound =>
          Future.failed(RetriableException(s"File containing output data for workflow " +
            s"s$workflowId and node s$nodeId not found", None))
        case StatusCodes.OK =>
          Future.successful(resp.entity.data.toByteArray)
        case statusCode =>
          Future.failed(NotebookHttpException(resp, s"Notebook server responded with $statusCode " +
            s"when asked for file for workflow $workflowId and node $nodeId"))
      }
    }
  }
}

object NotebookPoller {
  def apply(
      notebookRestClient: NotebookRestClient,
      pollInterval: FiniteDuration,
      retryCountLimit: Int,
      workflowId: Id,
      nodeId: Id,
      endpointPath: String
  )(implicit as: ActorSystem, tout: Timeout): Retry[Array[Byte]] = new NotebookPoller(
    notebookRestClient,
    pollInterval,
    retryCountLimit,
    workflowId,
    nodeId,
    endpointPath
  )
}
