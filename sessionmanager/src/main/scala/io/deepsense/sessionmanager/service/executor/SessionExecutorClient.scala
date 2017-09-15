/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.executor

import akka.actor.ActorRef

import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowexecutor.communication.message.global.{Launch, PoisonPill}


class SessionExecutorClient(val id: Workflow.Id, private val publisher: ActorRef) {
  def launchWorkflow(): Unit = {
    publisher ! Launch(id, Set())
  }

  def sendPoisonPill(): Unit = {
    publisher ! PoisonPill()
  }
}
