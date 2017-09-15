/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app

import com.google.inject.AbstractModule
import com.google.inject.binder.LinkedBindingBuilder
import com.google.inject.multibindings.Multibinder

import io.deepsense.experimentmanager.app.rest.{OperationsApi, ExperimentsApi}
import io.deepsense.experimentmanager.auth.AuthModule
import io.deepsense.experimentmanager.rest.RestComponent

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
    bindApi.to(classOf[ExperimentsApi])
    bindApi.to(classOf[OperationsApi])
  }
}
