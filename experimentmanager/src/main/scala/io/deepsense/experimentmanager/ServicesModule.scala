/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager

import com.google.inject.AbstractModule
import com.google.inject.assistedinject.FactoryModuleBuilder
import com.typesafe.config.ConfigFactory

import io.deepsense.experimentmanager.execution.{MockRunningExperimentsActorModule, RunningExperimentsActorModule}
import io.deepsense.experimentmanager.storage.ExperimentStorageModule

/**
 * Configures services.
 */
class ServicesModule extends AbstractModule {
  override def configure(): Unit = {
    val config = ConfigFactory.load
    if (Option(config.getBoolean("runningexperiments.override.with.mock")).getOrElse(false)) {
      install(new MockRunningExperimentsActorModule)
    } else {
      install(new RunningExperimentsActorModule)
    }
    install(new ExperimentStorageModule)
    install(new FactoryModuleBuilder()
      .implement(classOf[ExperimentManager], classOf[ExperimentManagerImpl])
      .build(classOf[ExperimentManagerProvider]))
  }
}
