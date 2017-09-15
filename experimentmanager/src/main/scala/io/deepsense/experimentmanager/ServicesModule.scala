/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager

import com.google.inject.AbstractModule
import com.google.inject.assistedinject.FactoryModuleBuilder
import com.typesafe.config.ConfigFactory

import io.deepsense.experimentmanager.execution.{ExecutionModule, MockRunningExperimentsActorModule, RunningExperimentsActorModule}
import io.deepsense.experimentmanager.storage.ExperimentStorageModule

/**
 * Configures services.
 */
class ServicesModule extends AbstractModule {
  override def configure(): Unit = {
    install(new ExecutionModule)
    install(new ExperimentStorageModule)
    install(new FactoryModuleBuilder()
      .implement(classOf[ExperimentManager], classOf[ExperimentManagerImpl])
      .build(classOf[ExperimentManagerProvider]))
  }
}
