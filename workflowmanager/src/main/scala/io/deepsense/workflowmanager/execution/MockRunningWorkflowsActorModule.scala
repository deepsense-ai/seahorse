/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.execution

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides, Singleton}

import io.deepsense.commons.akka.GuiceAkkaExtension

class MockRunningWorkflowsActorModule extends AbstractModule {
  override def configure(): Unit = {
  }

  @Provides
  @Singleton
  @Named("RunningExperimentsActor")
  def provideRunningExperimentsActorRef(system: ActorSystem): ActorRef = {
    system.actorOf(GuiceAkkaExtension(system).props[MockRunningWorkflowsActor])
  }
}
