/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage

import com.google.inject.AbstractModule


class WorkflowInMemoryStorageModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[WorkflowStorage]).to(classOf[InMemoryWorkflowStorage]).asEagerSingleton()
  }
}
