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

package ai.deepsense.seahorse.scheduling.server

import com.typesafe.config.ConfigFactory
import org.eclipse.jetty.server.Server

import ai.deepsense.commons.service.server.{CommonJettyMain, JettyConfig}
import ai.deepsense.seahorse.scheduling.SchedulingManagerConfig
import ai.deepsense.seahorse.scheduling.db.{Database, FlywayMigration}

object JettyMain {

  def main(args: Array[String]): Unit = start(args, SchedulingManagerConfig.jetty)

  def start(args: Array[String], config: JettyConfig): Server = {
    Database.forceInitialization()
    FlywayMigration.run()

    CommonJettyMain.startServer(
      contextPath = "/schedulingmanager/v1/",
      scalatraBootstrapClass = classOf[ScalatraBootstrap],
      webAppResourcePath = "scalatra-webapp",
      config
    )
  }

}
