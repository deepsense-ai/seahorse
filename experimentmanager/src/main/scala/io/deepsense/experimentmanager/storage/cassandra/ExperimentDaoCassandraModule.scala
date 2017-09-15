/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.storage.cassandra

import com.google.inject.{PrivateModule, Scopes}

import io.deepsense.commons.cassandra.CassandraFactoriesModule
import io.deepsense.experimentmanager.deeplang.DeepLangModule
import io.deepsense.experimentmanager.rest.json.GraphReaderModule
import io.deepsense.experimentmanager.storage.ExperimentStorage

class ExperimentDaoCassandraModule extends PrivateModule {
  override def configure(): Unit = {
    install(new CassandraFactoriesModule)
    install(new ExperimentSessionModule)
    bind(classOf[ExperimentStorage])
      .to(classOf[ExperimentDaoCassandraImpl])
      .in(Scopes.SINGLETON)
    expose(classOf[ExperimentStorage])
  }
}
