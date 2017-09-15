/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage

import com.google.inject.AbstractModule
import com.google.inject.binder.LinkedBindingBuilder
import com.google.inject.multibindings.Multibinder
import com.google.inject.name.Names.named

import io.deepsense.commons.auth.AuthModule
import io.deepsense.commons.rest.{RestComponent, VersionApi}
import io.deepsense.entitystorage.api.rest.EntitiesApi

/**
 * Configures all existing APIs.
 */
class ApisModule(withMockedSecurity: Boolean) extends AbstractModule {
  private lazy val apiBinder = Multibinder.newSetBinder(binder(), classOf[RestComponent])

  protected[this] def bindApi: LinkedBindingBuilder[RestComponent] = {
    apiBinder.addBinding()
  }

  override def configure(): Unit = {
    bind(classOf[String]).annotatedWith(named("componentName")).toInstance("entitystorage")

    install(new AuthModule(withMockedSecurity))
    bindApi.to(classOf[EntitiesApi])
    bindApi.to(classOf[VersionApi])
  }
}
