/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.rest

import com.google.inject.AbstractModule
import com.google.inject.binder.LinkedBindingBuilder
import com.google.inject.multibindings.Multibinder

import io.deepsense.commons.rest.RestComponent

class ApisModule extends AbstractModule {
  private lazy val apiBinder = Multibinder.newSetBinder(binder(), classOf[RestComponent])

  protected[this] def bindApi: LinkedBindingBuilder[RestComponent] = {
    apiBinder.addBinding()
  }

  override def configure(): Unit = {
    bindApi.to(classOf[SessionsApi])
  }
}
