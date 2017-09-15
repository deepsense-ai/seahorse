/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage.storage.cassandra

import com.google.inject.{PrivateModule, Scopes}

import io.deepsense.commons.cassandra.CassandraFactoriesModule
import io.deepsense.entitystorage.storage.EntityDao

class EntityDaoCassandraModule extends PrivateModule {
  override def configure(): Unit = {
    install(new CassandraFactoriesModule)
    install(new EntitySessionModule)
    bind(classOf[EntityDao])
      .to(classOf[EntityDaoCassandraImpl])
      .in(Scopes.SINGLETON)
    expose(classOf[EntityDao])
  }
}
