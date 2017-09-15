/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager

import com.google.inject.AbstractModule
import com.google.inject.assistedinject.FactoryModuleBuilder

import io.deepsense.workflowmanager.storage.cassandra.WorkflowDaoCassandraModule

/**
 * Configures services.
 */
class ServicesModule extends AbstractModule {
  override def configure(): Unit = {
    install(new WorkflowDaoCassandraModule)
    install(new FactoryModuleBuilder()
      .implement(classOf[WorkflowManager], classOf[WorkflowManagerImpl])
      .build(classOf[WorkflowManagerProvider]))
  }
}
