/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.livy

import java.util.concurrent.TimeUnit

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.util.Timeout
import com.google.inject.Inject
import com.google.inject.name.Named
import spray.client.pipelining._
import spray.http.{HttpRequest, _}
import spray.httpx.unmarshalling.FromResponseUnmarshaller

import io.deepsense.commons.models.Id
import io.deepsense.sessionmanager.service.livy.responses.{Batch, BatchList}

class DefaultLivy @Inject() (
  private val system: ActorSystem,
  @Named("livy-client.timeout") private val timeout: Int,
  @Named("livy-client.base-url") private val baseUrl: String,
  private val requestBuilder: RequestBodyBuilder
) extends LivyJsonProtocol with Livy {

  private implicit val implicitSystem = system
  private implicit val implicitTimeout = Timeout(timeout, TimeUnit.MILLISECONDS)
  private implicit val executionContext = system.dispatcher

  override def createSession(workflowId: Id): Future[Batch] = {
    createSessionPipeline(Post(
      batchesUrl,
      requestBuilder.createSession(workflowId)))
  }

  override def killSession(id: Int): Future[Boolean] = {
    killSessionPipeline(Delete(batchUrl(id)))
  }

  override def listSessions(): Future[BatchList] = {
    listSessionsPipeline(Get(batchesUrl))
  }

  override def getSession(id: Int): Future[Option[Batch]] = {
    getSessionPipeline(Get(batchUrl(id)))
  }

  private def createSessionPipeline: (HttpRequest) => Future[Batch] =
    sendReceive ~> unmarshal[Batch]

  private def killSessionPipeline: (HttpRequest) => Future[Boolean] = {
    sendReceive ~> convertToBoolean
  }

  private def listSessionsPipeline: (HttpRequest) => Future[BatchList] =
    sendReceive ~> unmarshal[BatchList]

  private def getSessionPipeline: (HttpRequest) => Future[Option[Batch]] = {
    sendReceive ~> convertOption[Batch]
  }

  private def convertToBoolean: Future[HttpResponse] => Future[Boolean] = {
    (futRes: Future[HttpResponse]) => futRes.map {
      _.status != StatusCodes.NotFound
    }
  }

  private type ConvertOptionFunction[T] = Future[HttpResponse] => Future[Option[T]]

  private def convertOption[T](
    implicit unmarshaller: FromResponseUnmarshaller[T]): ConvertOptionFunction[T] = {
    (futRes: Future[HttpResponse]) => futRes.map {
      res =>
        if (res.status == StatusCodes.NotFound) {
          None
        } else {
          Some(unmarshal[T](unmarshaller)(res))
        }
    }
  }

  private def batchesUrl: String = s"$baseUrl/batches"
  private def batchUrl(id: Int): String = s"$batchesUrl/$id"
}
