/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import io.deepsense.deeplang.{DOperable, ExecutionContext}

trait DOperableMock extends DOperable {
  override def toInferrable: DOperable = ???
  override def report: Report = ???
  override def save(executionContext: ExecutionContext)(path: String): Unit = ???
}
