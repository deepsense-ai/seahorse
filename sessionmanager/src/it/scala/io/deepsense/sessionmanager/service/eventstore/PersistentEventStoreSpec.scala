/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.eventstore

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

import slick.driver.H2Driver
import slick.driver.H2Driver.api.Database

import io.deepsense.sessionmanager.service.EventStore


class PersistentEventStoreSpec extends EventStoreSpec {
  override protected def eventStoreNoEvents(): EventStore = {
    val tableName = s"events_${Random.nextLong()}"

    val store = new PersistentEventStore(
      Database.forURL("jdbc:h2:mem:test;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1"),
      H2Driver,
      tableName)

    Await.result(store.create(), 2.seconds)
    store
  }
}
