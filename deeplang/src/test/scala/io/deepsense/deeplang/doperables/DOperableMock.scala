/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import io.deepsense.deeplang.{ExecutionContext, DOperable}

trait DOperableMock extends DOperable {
  override def report: Report = ???
  override def save(executionContext: ExecutionContext)(path: String): Unit = ???
}
