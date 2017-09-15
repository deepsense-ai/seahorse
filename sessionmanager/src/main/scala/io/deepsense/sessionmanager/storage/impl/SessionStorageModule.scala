/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.storage.impl

import com.google.inject.name.Names
import com.google.inject.{PrivateModule, Scopes}
import slick.driver.H2Driver.api.Database
import slick.driver.{H2Driver, JdbcDriver}

import io.deepsense.sessionmanager.storage.SessionStorage

class SessionStorageModule extends PrivateModule {
  override def configure(): Unit = {
    bind(classOf[JdbcDriver])
      .annotatedWith(Names.named("SessionManager"))
      .toInstance(H2Driver)
    bind(classOf[JdbcDriver#API#Database])
      .annotatedWith(Names.named("SessionManager"))
      .toInstance(Database.forConfig("h2"))

    bind(classOf[SessionStorage])
      .to(classOf[SessionStorageImpl])
      .in(Scopes.SINGLETON)
    expose(classOf[SessionStorage])
  }
}
