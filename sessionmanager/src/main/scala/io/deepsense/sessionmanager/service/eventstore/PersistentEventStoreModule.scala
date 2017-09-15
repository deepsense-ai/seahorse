/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.eventstore

import com.google.inject.{PrivateModule, Scopes}
import com.google.inject.name.Names
import slick.driver.{H2Driver, JdbcDriver}
import slick.driver.H2Driver.api.Database

import io.deepsense.sessionmanager.service.EventStore

class PersistentEventStoreModule extends PrivateModule {
  override def configure(): Unit = {

    bind(classOf[JdbcDriver])
      .annotatedWith(Names.named("EventStore"))
      .toInstance(H2Driver)
    bind(classOf[JdbcDriver#API#Database])
      .annotatedWith(Names.named("EventStore"))
      .toInstance(Database.forConfig("h2"))

    bind(classOf[EventStore])
      .to(classOf[PersistentEventStore])
      .in(Scopes.SINGLETON)
    expose(classOf[EventStore])
  }
}
