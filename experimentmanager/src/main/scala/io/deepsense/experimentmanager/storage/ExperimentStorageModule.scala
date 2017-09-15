/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.experimentmanager.storage

import com.google.inject.AbstractModule

class ExperimentStorageModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ExperimentStorage]).to(classOf[InMemoryExperimentStorage]).asEagerSingleton()
  }
}
