/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager

import com.google.inject.Module

import io.deepsense.commons.{IntegTestSupport, StandardSpec}

trait WorkflowManagerIntegTestSupport extends IntegTestSupport {
  suite: StandardSpec =>

  override protected def appGuiceModule: Module =
    new WorkflowManagerAppModule(withMockedSecurity = false)
}
