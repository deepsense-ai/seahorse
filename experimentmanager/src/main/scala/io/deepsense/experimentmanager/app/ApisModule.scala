/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app

import net.codingwell.scalaguice.ScalaModule.ScalaLinkedBindingBuilder
import net.codingwell.scalaguice.{ScalaModule, ScalaMultibinder}

import io.deepsense.experimentmanager.app.rest.RestApi
import io.deepsense.experimentmanager.auth.AuthModule
import io.deepsense.experimentmanager.rest.RestComponent

/**
 * Configures all existing APIs.
 */
class ApisModule extends ScalaModule {
  private lazy val apiBinder = ScalaMultibinder.newSetBinder[RestComponent](binder)

  protected[this] def bindApi: ScalaLinkedBindingBuilder[RestComponent] = {
    apiBinder.addBinding()
  }

  override def configure(): Unit = {
    install(new AuthModule)
    bindApi.to[RestApi]
  }
}
