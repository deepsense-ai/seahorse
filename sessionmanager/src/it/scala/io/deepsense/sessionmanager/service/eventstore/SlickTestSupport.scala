/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.eventstore

import scala.concurrent.duration._

import org.scalatest.time.{Millis, Seconds, Span}
import slick.driver.H2Driver.api._
import slick.driver.{H2Driver, JdbcDriver}

import io.deepsense.commons.StandardSpec

trait SlickTestSupport {

  suite: StandardSpec =>

  implicit override val patienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(5, Millis))

  val operationDuration = new FiniteDuration(5, SECONDS)

  val db: Database = Database.forConfig("db")

  val driver: JdbcDriver = H2Driver

  val sessionTableName = "SESSIONS"
}
