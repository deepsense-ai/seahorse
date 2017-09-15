/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager

import com.google.inject.Guice

import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.experimentmanager.rest.RestServer

/**
 * This is the entry point of the Experiment Manager application.
 */
object ExperimentManagerApp extends App {
  val injector = Guice.createInjector(new ExperimentManagerAppModule)

  CatalogRecorder.registerDOperables(injector.getInstance(classOf[DOperableCatalog]))
  CatalogRecorder.registerDOperations(injector.getInstance(classOf[DOperationsCatalog]))

  injector.getInstance(classOf[RestServer]).start()
}
