/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.schedule

import java.util.UUID

import akka.actor.ActorSystem

import io.deepsense.seahorse.scheduling.SchedulingManagerConfig

private[schedule] object RunWorkflowJobContext {
  private[this] val config = SchedulingManagerConfig.config

  val actorSystem = ActorSystem("scheduling-manager-run-workflow-job", config)

  val userId = UUID.fromString(config.getString("predefined-users.scheduler.id"))
  val userName = config.getString("predefined-users.scheduler.name")

  def generateWorkflowUrl(uuid: UUID): String =
    config.getString("scheduling-manager.workflowUrlPattern").format(uuid.toString)
}
