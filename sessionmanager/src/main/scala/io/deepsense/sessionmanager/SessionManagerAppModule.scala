/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager

import com.google.inject.AbstractModule

import io.deepsense.commons.akka.AkkaModule
import io.deepsense.commons.config.ConfigModule
import io.deepsense.commons.rest.RestModule
import io.deepsense.sessionmanager.mq.MqModule
import io.deepsense.sessionmanager.rest.ApisModule
import io.deepsense.sessionmanager.service.ServiceModule
import io.deepsense.sessionmanager.storage.impl.SessionStorageModule

class SessionManagerAppModule extends AbstractModule {
  override def configure(): Unit = {
    installCore()
    installServices()
    installServer()
  }

  private def installCore(): Unit = {
    install(new ConfigModule)
    install(new AkkaModule)
    install(new MqModule)
  }

  private def installServices(): Unit = {
    install(new ServiceModule)
    install(new SessionStorageModule)
  }

  private def installServer(): Unit = {
    install(new RestModule)
    install(new ApisModule)
  }
}
