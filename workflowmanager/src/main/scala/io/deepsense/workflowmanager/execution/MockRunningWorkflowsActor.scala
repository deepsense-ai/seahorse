/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.execution

import akka.actor.{Actor, ActorLogging}
import com.google.inject.Inject
import com.google.inject.name.Named

class MockRunningWorkflowsActor @Inject()(
    @Named("runningexperiments.mock.failureprobability") failureProbability: Double,
    @Named("runningexperiments.mock.tickdelay") tickDelay: Long)
  extends Actor
  with ActorLogging {

  override def receive: Receive = {
    case x => unhandled(x)
  }
}
