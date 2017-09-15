/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage

import akka.actor.ActorRef
import com.google.inject.name.Names
import com.google.inject.{AbstractModule, Key}

import io.deepsense.commons.akka.AkkaModule
import io.deepsense.commons.config.ConfigModule
import io.deepsense.commons.jclouds.{KeystoneApiModule, TokenApiModule}
import io.deepsense.commons.rest.RestModule
import io.deepsense.entitystorage.api.akka.EntitiesApiActorProvider
import io.deepsense.entitystorage.storage.cassandra.EntityDaoCassandraModule

/**
 * The main module for Entity Storage. Installs all needed modules to run
 * the application.
 */
class EntityStorageAppModule(withMockedSecurity: Boolean) extends AbstractModule {
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
    bind(Key.get(classOf[ActorRef], Names.named("EntitiesApiActor")))
      .toProvider(classOf[EntitiesApiActorProvider]).asEagerSingleton()
  }

  private def installServer(): Unit = {
    install(new RestModule)
    install(new ApisModule(withMockedSecurity))
  }
}
