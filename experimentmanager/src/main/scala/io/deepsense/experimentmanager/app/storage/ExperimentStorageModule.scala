/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.storage

import net.codingwell.scalaguice.ScalaModule

class ExperimentStorageModule extends ScalaModule {
  override def configure(): Unit = {
    bind[ExperimentStorage].to[MockedExperimentStorage].asEagerSingleton()
  }
}
