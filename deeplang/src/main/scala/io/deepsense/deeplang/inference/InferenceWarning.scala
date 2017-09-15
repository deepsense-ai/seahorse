/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.inference

import io.deepsense.deeplang.doperables.dataframe.{ColumnMetadata, DataFrameMetadata}
import io.deepsense.deeplang.parameters.ColumnType.ColumnType
import io.deepsense.deeplang.parameters.{SingleColumnSelection, ColumnSelection}

/**
 * Represents possibility that some exception will be thrown upon execution.
 */
abstract class InferenceWarning(val message: String)


case class MultipleColumnsMayNotExistWarning(
    selection: ColumnSelection,
    metadata: DataFrameMetadata)
  extends InferenceWarning(
    s"Column from specified selection: $selection may not exist in $metadata")

case class SingleColumnMayNotExistWarning(
    selection: SingleColumnSelection,
    metadata: DataFrameMetadata)
  extends InferenceWarning(
    s"Column from specified selection: $selection may not exist in $metadata")

case class ConversionMayNotBePossibleWarning(
    columnMetadata: ColumnMetadata,
    expectedType: ColumnType)
  extends InferenceWarning(
    s"Column ${columnMetadata.name} with type ${columnMetadata.columnType}" +
      s" may not be convertible to $expectedType")

