/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations.exceptions

import io.deepsense.deeplang.parameters.ColumnType.ColumnType


case class WrongColumnTypeException(
    columnName: String,
    actualType: ColumnType,
    expectedType: ColumnType)
  extends DOperationExecutionException(
    s"Column '$columnName' has type $actualType instead of expected $expectedType",
    None)
