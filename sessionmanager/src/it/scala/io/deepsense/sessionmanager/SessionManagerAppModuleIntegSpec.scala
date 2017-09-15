/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager

import com.google.inject.Guice

import io.deepsense.commons.StandardSpec
import io.deepsense.sessionmanager.rest.SessionsApi

class SessionManagerAppModuleIntegSpec extends StandardSpec {

  val injector = Guice.createInjector(new SessionManagerAppModule())

  "Guice context" should {
    "have all needed services created" in {
      assert(injector.getInstance(classOf[SessionsApi]) != null)
    }
  }
}
