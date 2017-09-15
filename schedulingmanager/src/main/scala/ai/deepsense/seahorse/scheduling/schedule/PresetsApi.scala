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

package ai.deepsense.seahorse.scheduling.schedule

import java.net.URL
import java.util.concurrent.TimeUnit

import akka.util.Timeout
import spray.http.BasicHttpCredentials

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

import ai.deepsense.commons.models.ClusterDetails
import ai.deepsense.commons.utils.LoggerForCallerClass
import ai.deepsense.seahorse.scheduling.SchedulingManagerConfig
import ai.deepsense.workflowmanager.client.PresetsClient

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
