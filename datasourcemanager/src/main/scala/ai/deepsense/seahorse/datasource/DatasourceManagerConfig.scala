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

package ai.deepsense.seahorse.datasource

import com.typesafe.config.ConfigFactory

import ai.deepsense.commons.service.db.DatabaseConfig
import ai.deepsense.commons.service.server.JettyConfig

object DatasourceManagerConfig {

  // TODO Load all *.default.conf automatically
  val config =
  ConfigFactory.defaultApplication().withFallback(
    ConfigFactory.load("database.default.conf")
  ).withFallback(
    ConfigFactory.load("jetty.default.conf")
  )

  val jetty = JettyConfig(config.getConfig("jetty"))
  val database = new DatabaseConfig(config.getConfig("database"))

  val predefinedUsersConfigPath = "predefined-users"
  val schedulerUserConfigPath = "scheduler"

}
