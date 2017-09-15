/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.commons.cassandra

import com.google.inject.{AbstractModule, Scopes}

class CassandraFactoriesModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ClusterFactory]).in(Scopes.SINGLETON)
    bind(classOf[SessionFactory]).in(Scopes.SINGLETON)
  }
}
