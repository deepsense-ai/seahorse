/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app

import com.google.inject.assistedinject.FactoryModuleBuilder
import net.codingwell.scalaguice.ScalaModule

import io.deepsense.experimentmanager.app.storage.ExperimentStorageModule

/**
 * Configures services.
 */
class ServicesModule extends ScalaModule {
  override def configure(): Unit = {
    install(new ExperimentStorageModule)
    install(new FactoryModuleBuilder()
      .implement(classOf[ExperimentManager], classOf[ExperimentManagerImpl])
      .build(classOf[ExperimentManagerProvider]))
  }
}
