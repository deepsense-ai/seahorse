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

import ai.deepsense.commons.utils.LoggerForCallerClass
import ai.deepsense.models.workflows.{Workflow, WorkflowInfo}
import ai.deepsense.seahorse.scheduling.SchedulingManagerConfig
import ai.deepsense.workflowmanager.client.WorkflowManagerClient
import ai.deepsense.workflowmanager.model.WorkflowDescription

private[schedule] object WorkflowsApi {
  private[this] val logger = LoggerForCallerClass()
  private[this] lazy val workflowManagerConfig = SchedulingManagerConfig.config.getConfig("workflow-manager")
  private[this] lazy val workflowClient = {
    val url = new URL(workflowManagerConfig.getString("url") + "/v1/workflows/")
    logger.info(s"Creating workflows client with url $url")
    val timeout = Timeout(FiniteDuration(workflowManagerConfig.getDuration("timeout").toMillis, TimeUnit.MILLISECONDS))
    new WorkflowManagerClient(
      apiUrl = url,
      mandatoryUserId = RunWorkflowJobContext.userId,
      mandatoryUserName = RunWorkflowJobContext.userName,
      credentials = Some(BasicHttpCredentials(
        workflowManagerConfig.getString("user"),
        workflowManagerConfig.getString("pass"))))(
      RunWorkflowJobContext.actorSystem, timeout)
  }

  def getWorkflowInfo(id: Workflow.Id): Future[WorkflowInfo] = {
    workflowClient.fetchWorkflowInfo(id)
  }

  def cloneWorkflow(id: Workflow.Id, workflowInfo: WorkflowInfo): Future[Workflow.Id] = {
    logger.info(s"Cloning workflow $id")
    workflowClient.cloneWorkflow(id, WorkflowDescription(
      s"""Scheduled execution of "${workflowInfo.name}"""",
      workflowInfo.description))
  }
}
