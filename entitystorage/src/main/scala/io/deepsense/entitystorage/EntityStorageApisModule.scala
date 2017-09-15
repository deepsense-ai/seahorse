/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.entitystorage

import com.google.inject.AbstractModule
import com.google.inject.binder.LinkedBindingBuilder
import com.google.inject.multibindings.Multibinder

import io.deepsense.commons.auth.AuthModule
import io.deepsense.commons.rest.RestComponent
import io.deepsense.entitystorage.api.rest.EntitiesApi

/**
 * Configures all existing APIs.
 */
class ApisModule extends AbstractModule {
  private lazy val apiBinder = Multibinder.newSetBinder(binder(), classOf[RestComponent])

  protected[this] def bindApi: LinkedBindingBuilder[RestComponent] = {
    apiBinder.addBinding()
  }

  override def configure(): Unit = {
    install(new AuthModule)
    bindApi.to(classOf[EntitiesApi])
  }
}
