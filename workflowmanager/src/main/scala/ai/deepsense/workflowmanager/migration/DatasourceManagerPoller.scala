/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.workflowmanager.migration

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

import akka.actor.ActorSystem
import akka.util.Timeout
import spray.client.pipelining._
import spray.http.StatusCodes.Success

import ai.deepsense.commons.rest.client.RestClient
import ai.deepsense.commons.utils.Retry
import ai.deepsense.commons.utils.RetryActor.RetriableException

class DatasourceManagerPoller(
    override val actorSystem: ActorSystem,
    val datasourceClient: RestClient)
  extends Retry[Unit] {

  implicit val ec: ExecutionContext = actorSystem.dispatcher

  override def work: Future[Unit] = {
    datasourceClient.fetchHttpResponse(Get(datasourceClient.endpointPath(""))).flatMap { resp =>
      resp.status match {
        case Success(_) => Future.successful(())
        case _ => Future.failed(RetriableException(s"received ${resp.status} from datasource manager server", None))
      }
    }
  }

  override val retryInterval: FiniteDuration = 1 second

  override val retryLimit: Int = 600

  override implicit val timeout: Timeout = 15 minutes

  override val workDescription: Option[String] = Some("wait for datasource manager")
}

object DatasourceManagerPoller {
  def apply(actorSystem: ActorSystem, datasourceClient: RestClient): DatasourceManagerPoller =
    new DatasourceManagerPoller(actorSystem, datasourceClient)
}
