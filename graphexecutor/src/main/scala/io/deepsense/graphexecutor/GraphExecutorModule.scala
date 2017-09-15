/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Chilkiewicz
 */
package io.deepsense.graphexecutor

import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides, Scopes, Singleton}

import io.deepsense.entitystorage.{EntityStorageClientFactory, EntityStorageClientFactoryImpl}

class GraphExecutorModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[EntityStorageClientFactory])
      .to(classOf[EntityStorageClientFactoryImpl])
      .in(Scopes.SINGLETON)
  }

  @Provides
  @Singleton
  @Named("default")
  def provide: EntityStorageClientFactory = {
    new EntityStorageClientFactoryImpl
  }
}
