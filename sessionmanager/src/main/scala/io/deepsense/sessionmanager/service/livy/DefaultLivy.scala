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

import io.deepsense.commons.models.Id
import io.deepsense.sessionmanager.service.livy.responses.Batch

class DefaultLivy @Inject() (
  private val system: ActorSystem,
  @Named("livy-client.timeout") private val timeout: Int,
  @Named("livy-client.base-url") private val baseUrl: String,
  private val requestBuilder: RequestBodyBuilder
) extends LivyJsonProtocol with Livy {

  private implicit val implicitSystem = system
  private implicit val implicitTimeout = Timeout(timeout, TimeUnit.MILLISECONDS)
  private implicit val executionContext = system.dispatcher

  private val credentials = BasicHttpCredentials("codilime", "Seahorse123")

  override def createSession(workflowId: Id): Future[Batch] = {
    createSessionPipeline(Post(
      batchesUrl,
      requestBuilder.createSession(workflowId)))
  }

  private def createSessionPipeline: (HttpRequest) => Future[Batch] =
    basePipeline ~> unmarshal[Batch]


  private def basePipeline = {
    addCredentials(credentials) ~> sendReceive
  }
  private type ConvertOptionFunction[T] = Future[HttpResponse] => Future[Option[T]]

  private def batchesUrl: String = s"$baseUrl/batches"
}
