/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager

import com.google.inject.{Scopes, AbstractModule}

import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.experimentmanager.akka.AkkaModule
import io.deepsense.experimentmanager.app.{ApisModule, ServicesModule}
import io.deepsense.experimentmanager.config.ConfigModule
import io.deepsense.experimentmanager.jclouds.{KeystoneApiModule, TokenApiModule}
import io.deepsense.experimentmanager.rest.RestModule

/**
 * The main module for Experiment Manager. Installs all needed modules to run
 * the application.
 */
class ExperimentManagerAppModule extends AbstractModule {
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
    install(new ApisModule)
  }
}
