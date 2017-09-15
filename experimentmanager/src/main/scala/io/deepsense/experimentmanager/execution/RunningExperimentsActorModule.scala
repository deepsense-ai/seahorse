/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.execution

import akka.actor.{ActorRef, ActorSystem, Props}
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides, Singleton}

import io.deepsense.graphexecutor.clusterspawner.{ClusterSpawner, ClusterSpawnerModule}

class RunningExperimentsActorModule extends AbstractModule {
  override def configure(): Unit = {
    install(new ClusterSpawnerModule())
  }

  @Provides
  @Singleton
  @Named("RunningExperimentsActor")
  def provideRunningExperimentsActorRef(
      @Named("entitystorage.label") entityStorageLabel: String,
      @Named("runningexperiments.timeout") timeoutMillis: Long,
      spawner: ClusterSpawner,
      system: ActorSystem): ActorRef = {

    system.actorOf(
      Props(new RunningExperimentsActor(entityStorageLabel, timeoutMillis, spawner)
        with ProductionGraphExecutorClientFactory
      ),
      "RunningExperimentsActor"
    )
  }
}
