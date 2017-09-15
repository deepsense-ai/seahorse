/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager

import com.google.inject.assistedinject.FactoryModuleBuilder
import com.google.inject.{AbstractModule, Provider}

import io.deepsense.entitystorage.{EntityStorageClientFactoryImpl, EntityStorageClientFactory}
import io.deepsense.workflowmanager.execution.ExecutionModule
import io.deepsense.workflowmanager.storage.cassandra.WorkflowDaoCassandraModule

/**
 * Configures services.
 */
class ServicesModule extends AbstractModule {
  override def configure(): Unit = {
    install(new ExecutionModule)
    install(new WorkflowDaoCassandraModule)
    install(new FactoryModuleBuilder()
      .implement(classOf[WorkflowManager], classOf[WorkflowManagerImpl])
      .build(classOf[WorkflowManagerProvider]))

    bind(classOf[EntityStorageClientFactory])
      .toProvider(new Provider[EntityStorageClientFactory] {
      override def get() = new EntityStorageClientFactoryImpl()
    })
  }
}
