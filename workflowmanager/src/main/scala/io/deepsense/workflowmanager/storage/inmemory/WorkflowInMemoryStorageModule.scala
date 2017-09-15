/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.inmemory

import com.google.inject.AbstractModule

import io.deepsense.workflowmanager.storage.WorkflowStorage


class WorkflowInMemoryStorageModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[WorkflowStorage]).to(classOf[InMemoryWorkflowStorage]).asEagerSingleton()
  }
}
