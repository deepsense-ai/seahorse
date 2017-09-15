/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.schedule

import java.net.URL
import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.util.Timeout
import spray.http.BasicHttpCredentials

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

import io.deepsense.commons.models.ClusterDetails
import io.deepsense.commons.utils.LoggerForCallerClass
import io.deepsense.graph.nodestate.name.NodeStatusName
import io.deepsense.models.workflows.{Workflow, WorkflowInfo}
import io.deepsense.seahorse.scheduling.SchedulingManagerConfig
import io.deepsense.seahorse.scheduling.mail.{EmailSender, EmailSenderConfig}
import io.deepsense.sessionmanager.rest.client.SessionManagerClient
import io.deepsense.sessionmanager.service.{Session, Status}
import io.deepsense.workflowmanager.client.{PresetsClient, WorkflowManagerClient}
import io.deepsense.workflowmanager.model.WorkflowDescription

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
