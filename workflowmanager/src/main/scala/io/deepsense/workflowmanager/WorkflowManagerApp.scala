/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager

import java.net.URL

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration

import akka.actor.ActorSystem
import com.google.inject.name.Names
import com.google.inject.{Guice, Key, Stage}

import io.deepsense.commons.rest.RestServer
import io.deepsense.commons.utils.Logging
import io.deepsense.workflowmanager.migration.Migration1_3To1_4
import io.deepsense.workflowmanager.storage.WorkflowStorage

/**
 * This is the entry point of the Workflow Manager application.
 */
object WorkflowManagerApp extends App with Logging {

  val insecure: Boolean = args.headOption.forall("insecure".equalsIgnoreCase)

  try {
    FlywayMigration.run()

    val injector = Guice.createInjector(Stage.PRODUCTION, new WorkflowManagerAppModule(insecure))
    val actorSystem = injector.getInstance(classOf[ActorSystem])
    implicit val ec: ExecutionContext = actorSystem.dispatcher

    val migration = Migration1_3To1_4(new URL(
      injector.getInstance(Key.get(classOf[String], Names.named("datasource-server.address")))),
      injector.getInstance(classOf[WorkflowStorage]),
      actorSystem
    )

    val migrationFut = for {
      _ <- migration.waitForDatasourceManager()
      _ <- migration.migrate()
    } yield {}

    migrationFut.onFailure {
      case t => logger.error("Migration 1.3 to 1.4 failed", t)
    }

    Await.ready(migrationFut, Duration.Inf)

    injector.getInstance(classOf[RestServer]).start()
    Await.result(actorSystem.whenTerminated, Duration.Inf)
  } catch {
    case e: Exception =>
      logger.error("Application context creation failed", e)
      System.exit(1)
  }
}
