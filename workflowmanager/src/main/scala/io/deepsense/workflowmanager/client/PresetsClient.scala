/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.workflowmanager.client

import java.net.URL
import java.util.UUID

import akka.actor.ActorSystem
import akka.util.Timeout
import spray.client.pipelining._
import spray.http._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

import io.deepsense.commons.models.ClusterDetails
import io.deepsense.commons.rest.ClusterDetailsJsonProtocol
import io.deepsense.commons.rest.client.RestClient
import io.deepsense.commons.utils.Logging

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
