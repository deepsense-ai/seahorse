/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations.exceptions
import io.deepsense.deeplang.parameters.ColumnType.ColumnType

case class TypeConversionException(
    actualType: ColumnType,
    targetType: ColumnType)
  extends DOperationExecutionException(
    s"Could not convert ${actualType.toString} to ${targetType.toString}.", None)
