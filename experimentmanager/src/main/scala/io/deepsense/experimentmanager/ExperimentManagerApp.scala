/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager

import akka.actor.ActorSystem
import com.google.inject.Guice

import io.deepsense.commons.rest.RestServer
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog

/**
 * This is the entry point of the Experiment Manager application.
 */
object ExperimentManagerApp extends App {
  val insecure: Boolean = args.headOption.map("insecure".equalsIgnoreCase(_)).getOrElse(false)
  val injector = Guice.createInjector(new ExperimentManagerAppModule(insecure))

  CatalogRecorder.registerDOperables(injector.getInstance(classOf[DOperableCatalog]))
  CatalogRecorder.registerDOperations(injector.getInstance(classOf[DOperationsCatalog]))

  injector.getInstance(classOf[RestServer]).start()
  injector.getInstance(classOf[ActorSystem]).awaitTermination()
  System.exit(1)
}
