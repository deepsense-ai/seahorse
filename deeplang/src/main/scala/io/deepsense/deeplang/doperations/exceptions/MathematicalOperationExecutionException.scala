/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations.exceptions

case class MathematicalOperationExecutionException(
    formula: String,
    rootCause: Option[Throwable])
  extends DOperationExecutionException(
    s"Problem while executing MathematicalOperation with given formula: $formula",
    rootCause)
