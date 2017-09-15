/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.schedule

import java.net.URL
import java.util.concurrent.TimeUnit

import akka.util.Timeout
import spray.http.BasicHttpCredentials

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

import io.deepsense.commons.models.ClusterDetails
import io.deepsense.commons.utils.LoggerForCallerClass
import io.deepsense.seahorse.scheduling.SchedulingManagerConfig
import io.deepsense.workflowmanager.client.PresetsClient

private[schedule] object PresetsApi {
  private[this] val logger = LoggerForCallerClass()
  private[this] val presetsConfig = SchedulingManagerConfig.config.getConfig("presets-manager")
  private[this] lazy val presetsClient = {
    val url = new URL(presetsConfig.getString("url") + "/v1/presets/")
    logger.info(s"Creating presets client with url $url")
    val timeout = Timeout(FiniteDuration(presetsConfig.getDuration("timeout").toMillis, TimeUnit.MILLISECONDS))
    new PresetsClient(
      apiUrl = url,
      mandatoryUserId = RunWorkflowJobContext.userId,
      mandatoryUserName = RunWorkflowJobContext.userName,
      credentials = Some(BasicHttpCredentials(
        presetsConfig.getString("user"),
        presetsConfig.getString("pass"))))(
      RunWorkflowJobContext.actorSystem, timeout)
  }

  def fetchPreset(id: Long): Future[Option[ClusterDetails]] = {
    logger.info(s"Fetching preset $id")
    presetsClient.fetchPreset(id)
  }
}
