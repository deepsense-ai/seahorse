/**
 * Copyright (c) 2017, CodiLime Inc.
 */

package io.deepsense.workflowmanager.migration

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

import akka.actor.ActorSystem
import akka.util.Timeout
import spray.client.pipelining._
import spray.http.StatusCodes.Success

import io.deepsense.commons.rest.client.RestClient
import io.deepsense.commons.utils.Retry
import io.deepsense.commons.utils.RetryActor.RetriableException

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
