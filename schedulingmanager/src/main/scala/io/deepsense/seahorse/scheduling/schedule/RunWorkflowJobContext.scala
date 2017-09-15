/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.schedule

import java.util.UUID

import akka.actor.ActorSystem

import io.deepsense.seahorse.scheduling.SchedulingManagerConfig

private[schedule] object RunWorkflowJobContext {
  val actorSystem = ActorSystem("scheduling-manager-run-workflow-job", SchedulingManagerConfig.config)
  private[this] val schedulingManagerUserConfig = SchedulingManagerConfig.config.getConfig("scheduling-manager")
  val userId = UUID.fromString(schedulingManagerUserConfig.getString("user.id"))
  val userName = schedulingManagerUserConfig.getString("user.name")
  def generateWorkflowUrl(uuid: UUID): String = {
    schedulingManagerUserConfig.getString("workflowUrlPattern").format(uuid.toString)
  }
}
