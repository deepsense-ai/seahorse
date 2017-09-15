/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.experimentmanager

import com.google.inject.AbstractModule
import com.google.inject.binder.LinkedBindingBuilder
import com.google.inject.multibindings.Multibinder

import io.deepsense.commons.auth.AuthModule
import io.deepsense.commons.rest.RestComponent
import io.deepsense.experimentmanager.deeplang.DeepLangModule
import io.deepsense.experimentmanager.rest.json.GraphReaderModule
import io.deepsense.experimentmanager.rest._

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
    install(new GraphReaderModule)
    install(new DeepLangModule)
    bindApi.to(classOf[ExperimentsApi])
    bindApi.to(classOf[OperationsApi])
    bindApi.to(classOf[ModelsApi])
  }
}
