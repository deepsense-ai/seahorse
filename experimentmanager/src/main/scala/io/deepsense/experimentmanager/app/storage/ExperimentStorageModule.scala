/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.storage

import com.google.inject.AbstractModule

class ExperimentStorageModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ExperimentStorage]).to(classOf[InMemoryExperimentStorage]).asEagerSingleton()
  }
}
