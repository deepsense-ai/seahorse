/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.rest

import com.google.inject.AbstractModule
import com.google.inject.binder.LinkedBindingBuilder
import com.google.inject.multibindings.Multibinder
import com.google.inject.name.Names._

import io.deepsense.commons.auth.AuthModule
import io.deepsense.commons.rest.{RestComponent, VersionApi}

class ApisModule extends AbstractModule {
  private lazy val apiBinder = Multibinder.newSetBinder(binder(), classOf[RestComponent])

  protected[this] def bindApi: LinkedBindingBuilder[RestComponent] = {
    apiBinder.addBinding()
  }

  override def configure(): Unit = {
    bind(classOf[String]).annotatedWith(named("componentName")).toInstance("sessionmanager")
    install(new AuthModule(useMockSecurity = true))
    bindApi.to(classOf[SessionsApi])
    bindApi.to(classOf[VersionApi])
  }
}
