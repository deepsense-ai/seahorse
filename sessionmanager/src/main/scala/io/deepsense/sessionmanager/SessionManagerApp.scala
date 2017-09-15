/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager

import akka.actor.ActorSystem
import com.google.inject.{Guice, Stage}
import io.deepsense.commons.rest.RestServer
import io.deepsense.commons.utils.Logging

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object SessionManagerApp extends App with Logging {
  try {
    FlywayMigration.run()

    // FIXME Guice modules have side effect. Simply getting instance starts actor system responsible
    // for listening heartbeats. Rework so module declarations are pure and side effects start here.

    val injector = Guice.createInjector(Stage.PRODUCTION, new SessionManagerAppModule)
    injector.getInstance(classOf[RestServer]).start()
    Await.result(injector.getInstance(classOf[ActorSystem]).whenTerminated, Duration.Inf)
  } catch {
    case e: Exception =>
      logger.error("Application context creation failed", e)
      System.exit(1)
  }
}
