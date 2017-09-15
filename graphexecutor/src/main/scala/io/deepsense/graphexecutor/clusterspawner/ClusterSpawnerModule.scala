/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphexecutor.clusterspawner

import com.google.inject.AbstractModule

class ClusterSpawnerModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ClusterSpawner])
      .toInstance(DefaultClusterSpawner)
  }
}
