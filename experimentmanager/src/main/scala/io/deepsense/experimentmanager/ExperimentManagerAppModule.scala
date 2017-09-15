/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager

import net.codingwell.scalaguice.ScalaModule

import io.deepsense.experimentmanager.akka.AkkaModule
import io.deepsense.experimentmanager.app.{ApisModule, ServicesModule}
import io.deepsense.experimentmanager.config.ConfigModule
import io.deepsense.experimentmanager.rest.RestModule

/**
 * The main module for Experiment Manager. Installs all needed modules to run
 * the application.
 */
class ExperimentManagerAppModule extends ScalaModule {
  override def configure(): Unit = {
    installCore()
    installServices()
    installServer()
  }

  private def installCore(): Unit = {
    install(new ConfigModule)
    install(new AkkaModule)
  }

  private def installServices(): Unit = {
    install(new ServicesModule)
  }

  private def installServer(): Unit = {
    install(new RestModule)
    install(new ApisModule)
  }
}
