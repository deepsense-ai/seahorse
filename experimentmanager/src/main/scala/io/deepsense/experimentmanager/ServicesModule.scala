/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager

import com.google.inject.AbstractModule
import com.google.inject.assistedinject.FactoryModuleBuilder

import io.deepsense.experimentmanager.execution.ExecutionModule
import io.deepsense.experimentmanager.storage.cassandra.ExperimentDaoCassandraModule

/**
 * Configures services.
 */
class ServicesModule extends AbstractModule {
  override def configure(): Unit = {
    install(new ExecutionModule)
    install(new ExperimentDaoCassandraModule)
    install(new FactoryModuleBuilder()
      .implement(classOf[ExperimentManager], classOf[ExperimentManagerImpl])
      .build(classOf[ExperimentManagerProvider]))
  }
}
