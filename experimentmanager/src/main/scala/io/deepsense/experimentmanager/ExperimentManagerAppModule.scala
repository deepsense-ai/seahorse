/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager

import com.google.inject.AbstractModule

import io.deepsense.commons.akka.AkkaModule
import io.deepsense.commons.config.ConfigModule
import io.deepsense.commons.jclouds.{KeystoneApiModule, TokenApiModule}
import io.deepsense.commons.rest.RestModule

/**
 * The main module for Experiment Manager. Installs all needed modules to run
 * the application.
 */
class ExperimentManagerAppModule(withMockedSecurity: Boolean) extends AbstractModule {
  override def configure(): Unit = {
    installCore()
    installServices()
    installServer()
  }

  private def installCore(): Unit = {
    install(new ConfigModule)
    install(new AkkaModule)
    install(new KeystoneApiModule)
    install(new TokenApiModule)
  }

  private def installServices(): Unit = {
    install(new ServicesModule)
  }

  private def installServer(): Unit = {
    install(new RestModule)
    install(new ApisModule(withMockedSecurity))
  }
}
