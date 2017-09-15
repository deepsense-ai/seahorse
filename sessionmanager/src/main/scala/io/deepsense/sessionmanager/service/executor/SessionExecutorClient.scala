/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.executor

import akka.actor.ActorRef

import io.deepsense.workflowexecutor.communication.message.global.PoisonPill

class SessionExecutorClient(private val publisher: ActorRef) {
  def sendPoisonPill(): Unit = {
    publisher ! PoisonPill()
  }
}
