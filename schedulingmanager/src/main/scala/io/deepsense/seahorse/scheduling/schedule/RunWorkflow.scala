/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.schedule

import scala.concurrent.Future

import io.deepsense.commons.utils.LoggerForCallerClass

// TODO This is a moack now - it should run workflow and send an email
class RunWorkflow extends WorkflowJob {
  private[this] val logger = LoggerForCallerClass()
  override def runWorkflow(workflowId: String, sendReportToEmail: String): Future[Unit] = {
    logger.info(s"Running $workflowId and sending report to $sendReportToEmail")
    Future.successful(())
  }
}
