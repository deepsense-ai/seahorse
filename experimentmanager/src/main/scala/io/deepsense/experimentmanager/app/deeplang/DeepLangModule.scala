/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.deeplang

import com.google.inject.AbstractModule

import io.deepsense.deeplang.InferContext
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog

/**
 * Provides InferContext and GraphReader. To do so,
 * creates DOperableCatalog and DOperationsCatalog.
 *
 * This module is just to satisfy dependencies. It should
 * be changed to provide valid catalogs as currently no
 * objects are registered in them.
 */
class DeepLangModule extends AbstractModule {
  override def configure(): Unit = {
    val dOperableCatalog = new DOperableCatalog
    val inferContext = new InferContext(dOperableCatalog)
    val dOperationsCatalog = DOperationsCatalog()
    bind(classOf[DOperationsCatalog]).toInstance(dOperationsCatalog)
    bind(classOf[InferContext]).toInstance(inferContext)
  }
}
