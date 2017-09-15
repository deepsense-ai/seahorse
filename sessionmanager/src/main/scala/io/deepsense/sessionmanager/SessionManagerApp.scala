/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager

import akka.actor.ActorSystem
import com.google.inject.{Stage, Guice}

import io.deepsense.commons.rest.RestServer
import io.deepsense.commons.utils.Logging

object SessionManagerApp extends App with Logging {
  try {
    FlywayMigration.run()

    val injector = Guice.createInjector(Stage.PRODUCTION, new SessionManagerAppModule)
    injector.getInstance(classOf[RestServer]).start()
    injector.getInstance(classOf[ActorSystem]).awaitTermination()
  } catch {
    case e: Exception =>
      logger.error("Application context creation failed", e)
      System.exit(1)
  }
}
