/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.eventstore
import io.deepsense.sessionmanager.service.EventStore

class InMemoryEventStoreSpec extends EventStoreSpec {
  override protected def eventStoreNoEvents(): EventStore = new InMemoryEventStore()
}
