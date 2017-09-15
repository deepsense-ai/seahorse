/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import akka.actor.ActorSystem
import com.google.inject.{Guice, Injector, Stage}

import io.deepsense.commons.rest.RestServer
import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.CatalogRecorder
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.deeplang.refl.CatalogScanner

/**
 * This is the entry point of the Workflow Manager application.
 */
object WorkflowManagerApp extends App with Logging {

  val insecure: Boolean = args.headOption.forall("insecure".equalsIgnoreCase)

  try {
    FlywayMigration.run()

    val injector = Guice.createInjector(Stage.PRODUCTION, new WorkflowManagerAppModule(insecure))
    injector.getInstance(classOf[RestServer]).start()
    Await.result(injector.getInstance(classOf[ActorSystem]).whenTerminated, Duration.Inf)
  } catch {
    case e: Exception =>
      logger.error("Application context creation failed", e)
      System.exit(1)
  }
}
