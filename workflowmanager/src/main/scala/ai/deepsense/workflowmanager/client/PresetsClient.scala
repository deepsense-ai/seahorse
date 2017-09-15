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

package ai.deepsense.workflowmanager.client

import java.net.URL
import java.util.UUID

import akka.actor.ActorSystem
import akka.util.Timeout
import spray.client.pipelining._
import spray.http._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

import ai.deepsense.commons.models.ClusterDetails
import ai.deepsense.commons.rest.ClusterDetailsJsonProtocol
import ai.deepsense.commons.rest.client.RestClient
import ai.deepsense.commons.utils.Logging

class PresetsClient(
    override val apiUrl: URL,
    mandatoryUserId: UUID,
    mandatoryUserName: String,
    override val credentials: Option[HttpCredentials])(
    implicit override val as: ActorSystem,
    override val timeout: Timeout)
  extends RestClient with ClusterDetailsJsonProtocol with Logging {

  override def userId: Option[UUID] = Some(mandatoryUserId)
  override def userName: Option[String] = Some(mandatoryUserName)

  def fetchPreset(id: Long): Future[Option[ClusterDetails]] = {
    fetchResponse[Option[ClusterDetails]](Get(endpointPath(s"$id")))
  }
}
