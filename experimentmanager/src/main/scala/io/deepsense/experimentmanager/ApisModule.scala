/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager

import com.google.inject.AbstractModule
import com.google.inject.binder.LinkedBindingBuilder
import com.google.inject.multibindings.Multibinder

import io.deepsense.commons.auth.AuthModule
import io.deepsense.commons.rest.{RestComponent, VersionApi}
import io.deepsense.experimentmanager.deeplang.DeepLangModule
import io.deepsense.experimentmanager.rest._
import io.deepsense.experimentmanager.rest.json.GraphReaderModule

/**
 * Configures all existing APIs.
 */
class ApisModule(withMockedSecurity: Boolean) extends AbstractModule {
  private lazy val apiBinder = Multibinder.newSetBinder(binder(), classOf[RestComponent])

  protected[this] def bindApi: LinkedBindingBuilder[RestComponent] = {
    apiBinder.addBinding()
  }

  override def configure(): Unit = {
    install(new AuthModule(withMockedSecurity))
    install(new GraphReaderModule)
    install(new DeepLangModule)
    bindApi.to(classOf[ExperimentsApi])
    bindApi.to(classOf[OperationsApi])
    bindApi.to(classOf[ModelsApi])
    bindApi.to(classOf[VersionApi])
  }
}
