/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.cassandra

import com.google.inject.{PrivateModule, Scopes}

import io.deepsense.commons.cassandra.CassandraFactoriesModule
import io.deepsense.workflowmanager.storage.{NotebookStorage, WorkflowResultsStorage, WorkflowStorage}

class WorkflowDaoCassandraModule extends PrivateModule {
  override def configure(): Unit = {
    install(new CassandraFactoriesModule)
    install(new WorkflowSessionModule)
    bind(classOf[WorkflowStorage])
      .to(classOf[WorkflowDaoCassandraImpl])
      .in(Scopes.SINGLETON)
    expose(classOf[WorkflowStorage])

    bind(classOf[WorkflowResultsStorage])
      .to(classOf[WorkflowResultsDaoCassandraImpl])
      .in(Scopes.SINGLETON)
    expose(classOf[WorkflowResultsStorage])

    bind(classOf[NotebookStorage])
      .to(classOf[NotebookDaoCassandraImpl])
      .in(Scopes.SINGLETON)
    expose(classOf[NotebookStorage])
  }
}
