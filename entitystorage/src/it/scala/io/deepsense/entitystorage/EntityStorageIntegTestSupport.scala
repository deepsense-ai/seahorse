/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage

import com.google.inject.Module

import io.deepsense.commons.{IntegTestSupport, StandardSpec}

trait EntityStorageIntegTestSupport extends IntegTestSupport {
  suite: StandardSpec =>

  override protected def appGuiceModule: Module =
    new EntityStorageAppModule(withMockedSecurity = false)
}
