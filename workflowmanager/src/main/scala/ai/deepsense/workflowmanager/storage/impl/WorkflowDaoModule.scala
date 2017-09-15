/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.deepsense.workflowmanager.storage.impl

import com.google.inject.name.Names
import com.google.inject.{PrivateModule, Scopes}
import slick.driver.H2Driver.api.Database
import slick.driver.{H2Driver, JdbcDriver}

import ai.deepsense.workflowmanager.storage.{NotebookStorage, WorkflowStateStorage, WorkflowStorage}

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
