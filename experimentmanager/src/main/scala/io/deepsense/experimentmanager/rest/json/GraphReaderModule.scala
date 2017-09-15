/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.rest.json

import com.google.inject.{AbstractModule, Provides, Singleton}

import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.graphjson.GraphJsonProtocol.GraphReader

class GraphReaderModule extends AbstractModule {
  override def configure(): Unit = {
    // Done by 'provides' methods.
  }

  @Singleton
  @Provides
  def provideGraphReader(dOperationsCatalog: DOperationsCatalog) = {
    new GraphReader(dOperationsCatalog)
  }
}
