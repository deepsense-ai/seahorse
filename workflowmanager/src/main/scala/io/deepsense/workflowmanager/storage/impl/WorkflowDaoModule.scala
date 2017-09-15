/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.impl

import com.google.inject.name.Names
import com.google.inject.{PrivateModule, Scopes}
import slick.driver.H2Driver.api.Database
import slick.driver.{H2Driver, JdbcDriver}

import io.deepsense.workflowmanager.storage.{NotebookStorage, WorkflowStateStorage, WorkflowStorage}

class WorkflowDaoModule extends PrivateModule {
  override def configure(): Unit = {
    bind(classOf[JdbcDriver])
      .annotatedWith(Names.named("workflowmanager"))
      .toInstance(H2Driver)
    bind(classOf[JdbcDriver#API#Database])
      .annotatedWith(Names.named("workflowmanager"))
      .toInstance(Database.forConfig("db"))

    bind(classOf[WorkflowStorage])
      .to(classOf[WorkflowDaoImpl])
      .in(Scopes.SINGLETON)
    expose(classOf[WorkflowStorage])

    bind(classOf[NotebookStorage])
      .to(classOf[NotebookDaoImpl])
      .in(Scopes.SINGLETON)
    expose(classOf[NotebookStorage])

    bind(classOf[WorkflowStateStorage])
      .to(classOf[WorkflowStateDaoImpl])
      .in(Scopes.SINGLETON)
    expose(classOf[WorkflowStateStorage])

    bind(classOf[PresetsDao])
      .in(Scopes.SINGLETON)
    expose(classOf[PresetsDao])
  }
}
