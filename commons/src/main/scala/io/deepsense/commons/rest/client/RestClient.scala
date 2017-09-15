/**
 * Copyright 2016, deepsense.io
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

package io.deepsense.commons.rest.client

import java.net.URL
import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}
import akka.io.IO
import akka.pattern.ask
import spray.client.pipelining._
import spray.can.Http
import spray.can.Http.HostConnectorInfo
import spray.http.{HttpCredentials, HttpRequest, HttpResponse}
import spray.httpx.unmarshalling.FromResponseUnmarshaller

trait RestClient extends RestClientImplicits {
  def apiUrl: URL
  def userId: Option[UUID]
  def userName: Option[String]
  def credentials: Option[HttpCredentials]
  implicit override val ctx: ExecutionContext = as.dispatcher

  private def hostConnectorFut(): Future[HostConnectorInfo] = {
    (IO(Http) ? Http.HostConnectorSetup(apiUrl.getHost, port = apiUrl.getPort)).mapTo[HostConnectorInfo]
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

  def endpointPath(endpoint: String): String = {
    new URL(apiUrl, endpoint).getFile
  }
}
