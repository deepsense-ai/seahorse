/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.execution

import io.deepsense.graphexecutor.GraphExecutorClient

class DefaultGraphExecutorClientFactory extends GraphExecutorClientFactory {
  override def create(): GraphExecutorClient = new GraphExecutorClient
}
