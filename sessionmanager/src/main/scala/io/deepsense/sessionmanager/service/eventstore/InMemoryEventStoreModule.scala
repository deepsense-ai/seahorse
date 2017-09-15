/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.eventstore

import com.google.inject.{PrivateModule, Scopes}

import io.deepsense.sessionmanager.service.EventStore

class InMemoryEventStoreModule extends PrivateModule {
  override def configure(): Unit = {
    bind(classOf[EventStore])
      .to(classOf[InMemoryEventStore])
      .in(Scopes.SINGLETON)
    expose(classOf[EventStore])
  }
}
