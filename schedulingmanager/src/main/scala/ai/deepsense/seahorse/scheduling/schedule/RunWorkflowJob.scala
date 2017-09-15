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

import java.util.UUID

import scala.concurrent.Future

import ai.deepsense.commons.utils.LoggerForCallerClass
import ai.deepsense.models.workflows.{Workflow, WorkflowInfo}

class RunWorkflowJob extends WorkflowJob {
  import scala.concurrent.ExecutionContext.Implicits.global

  private[this] val logger = LoggerForCallerClass()

  override def runWorkflow(workflowId: String, sendReportToEmail: String, presetId: Long): Future[Unit] = {
    logger.info(s"Starting workflow $workflowId scheduled execution on cluster $presetId with email " +
      s"to $sendReportToEmail afterwards.")

    // TODO Rewrite using queue/db
    // TODO For now, it's not very reliable - if something fails in the middle, whole thing fails to finish.
    val idToClone = Workflow.Id(UUID.fromString(workflowId))
    for {
      workflowInfo <- WorkflowsApi.getWorkflowInfo(idToClone)
      clonedId <- WorkflowsApi.cloneWorkflow(idToClone, workflowInfo)
      presetClusterDetailsOpt <- PresetsApi.fetchPreset(presetId)
      presetClusterDetails = presetClusterDetailsOpt
        .getOrElse(throw new IllegalArgumentException(s"Preset $presetId doesn't exist any more."))
      () <- SessionsApi.startSession(clonedId, presetClusterDetails)
      () <- SessionsApi.runWorkflow(clonedId)
      () <- SessionsApi.deleteSession(clonedId)
      () <- sendEmail(clonedId, sendReportToEmail, workflowInfo)
    } yield ()
  }

  private[this] def sendEmail(
      clonedId: Workflow.Id,
      email: String,
      originalWorkflowInfo: WorkflowInfo): Future[Unit] = {
    logger.info(s"Sending email, cloned workflow id: $clonedId, email: $email.")
    EmailSenderApi.sendEmail(s"""Scheduled execution of workflow "${originalWorkflowInfo.name}"""",
      s"Scheduled execution of your workflow has just been finished. " +
        s"You can see the report at ${RunWorkflowJobContext.generateWorkflowUrl(clonedId.value)}", email)
    Future.successful(())
  }
}
