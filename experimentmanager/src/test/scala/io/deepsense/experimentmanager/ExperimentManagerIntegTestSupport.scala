/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager

import com.google.inject.Module

import io.deepsense.commons.{IntegTestSupport, StandardSpec}

trait ExperimentManagerIntegTestSupport extends IntegTestSupport {
  suite: StandardSpec =>

  override protected def appGuiceModule: Module =
    new ExperimentManagerAppModule
}
