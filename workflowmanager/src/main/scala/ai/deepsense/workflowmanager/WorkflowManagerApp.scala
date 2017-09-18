/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.deepsense.workflowmanager

import java.net.URL

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

import akka.actor.ActorSystem
import com.google.inject.name.Names
import com.google.inject.{Guice, Key, Stage}

import ai.deepsense.commons.rest.RestServer
import ai.deepsense.commons.utils.Logging
import ai.deepsense.sparkutils.AkkaUtils
import ai.deepsense.workflowmanager.migration.{Migration1_3To1_4, SeahorseDbMigration}
import ai.deepsense.workflowmanager.storage.WorkflowStorage

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

    val datasourceUrl = new URL(injector.getInstance(Key.get(classOf[String],
      Names.named("datasource-server.address"))))


    val migrationFut =
    for {
      _ <- SeahorseDbMigration.waitForDatasourceManager(datasourceUrl, actorSystem)
      _ <- SeahorseDbMigration.migrate(
        datasourceUrl,
        injector.getInstance(classOf[WorkflowStorage]),
        actorSystem)
    } yield ()

    migrationFut.onFailure {
      case t => logger.error("Migration 1.3 to 1.4 failed", t)
    }

    Await.ready(migrationFut, Duration.Inf)

    injector.getInstance(classOf[RestServer]).start()
    AkkaUtils.awaitTermination(actorSystem)
  } catch {
    case e: Exception =>
      logger.error("Application context creation failed", e)
      System.exit(1)
  }

}
