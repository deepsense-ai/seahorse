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

package ai.deepsense.workflowmanager.storage.impl

import scala.concurrent.duration._

import org.scalatest.time.{Millis, Seconds, Span}
import slick.driver.H2Driver.api._
import slick.driver.{H2Driver, JdbcDriver}

import ai.deepsense.commons.StandardSpec

trait SlickTestSupport {

  suite: StandardSpec =>

  implicit override val patienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(5, Millis))

  val operationDuration = new FiniteDuration(5, SECONDS)

  val db: Database = Database.forConfig("db")

  val driver: JdbcDriver = H2Driver
}
