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

package ai.deepsense.seahorse.datasource.db

import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

import ai.deepsense.seahorse.datasource.DatasourceManagerConfig

object Database {

  val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("databaseSlick", DatasourceManagerConfig.config)

  val db = dbConfig.db
  val driver: JdbcProfile = dbConfig.driver
  val api = driver.api

  def forceInitialization(): Unit = {
    // Force initialization here to work around bug https://github.com/slick/slick/issues/1400
    val session = db.createSession()
    try session.force() finally session.close()
  }

}
