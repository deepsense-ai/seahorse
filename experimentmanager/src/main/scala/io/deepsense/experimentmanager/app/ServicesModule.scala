/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app

import net.codingwell.scalaguice.ScalaModule

/**
 * Configures services.
 */
class ServicesModule extends ScalaModule {
  override def configure(): Unit = {
    bind[ExperimentManager].to[ExperimentManagerImpl]
  }
}
