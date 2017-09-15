/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.sessionmanager

import akka.actor.ActorSystem
import com.google.inject.{Guice, Stage}
import ai.deepsense.commons.rest.RestServer
import ai.deepsense.commons.utils.Logging
import ai.deepsense.sparkutils.AkkaUtils

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object SessionManagerApp extends App with Logging {
  try {
    FlywayMigration.run()

    // FIXME Guice modules have side effect. Simply getting instance starts actor system responsible
    // for listening heartbeats. Rework so module declarations are pure and side effects start here.

    val injector = Guice.createInjector(Stage.PRODUCTION, new SessionManagerAppModule)
    injector.getInstance(classOf[RestServer]).start()
    AkkaUtils.awaitTermination(injector.getInstance(classOf[ActorSystem]))
  } catch {
    case e: Exception =>
      logger.error("Application context creation failed", e)
      System.exit(1)
  }
}
