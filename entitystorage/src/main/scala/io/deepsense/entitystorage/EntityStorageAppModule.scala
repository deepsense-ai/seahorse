/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage

import com.google.inject.AbstractModule

import io.deepsense.commons.akka.AkkaModule
import io.deepsense.commons.config.ConfigModule
import io.deepsense.commons.jclouds.{KeystoneApiModule, TokenApiModule}
import io.deepsense.commons.rest.RestModule
import io.deepsense.entitystorage.api.akka.EntitiesActorModule
import io.deepsense.entitystorage.storage.cassandra.EntityDaoCassandraModule

/**
 * The main module for Entity Storage. Installs all needed modules to run
 * the application.
 */
class EntityStorageAppModule extends AbstractModule {
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
    install(new EntityDaoCassandraModule)
    install(new EntitiesActorModule)
  }

  private def installServer(): Unit = {
    install(new RestModule)
    install(new ApisModule)
  }
}
