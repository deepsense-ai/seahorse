/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.sessionmanager.storage.inmemory

import com.google.inject.AbstractModule

import io.deepsense.sessionmanager.storage.SessionStorage


class SessionInMemoryStorageModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[SessionStorage]).to(classOf[InMemorySessionStorage]).asEagerSingleton()
  }
}
