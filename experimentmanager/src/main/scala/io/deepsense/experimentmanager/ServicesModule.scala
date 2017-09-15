/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager

import com.google.inject.assistedinject.FactoryModuleBuilder
import com.google.inject.{AbstractModule, Provider}

import io.deepsense.entitystorage.{EntityStorageClientFactoryImpl, EntityStorageClientFactory}
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

    bind(classOf[EntityStorageClientFactory])
      .toProvider(new Provider[EntityStorageClientFactory] {
      override def get() = new EntityStorageClientFactoryImpl()
    })
  }
}
